package com.profitcalculator.dascher.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitcalculator.dascher.dto.CostDTO;
import com.profitcalculator.dascher.dto.IncomeDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProfitIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  // ── URL helpers ───────────────────────────────────────────────────────────

  private String shipmentsUrl() {
    return "http://localhost:" + port + "/api/shipments";
  }

  private String shipmentUrl(Long id) {
    return shipmentsUrl() + "/" + id;
  }

  private String profitUrl() {
    return shipmentsUrl() + "/profit";
  }

  private String profitUrl(Long id) {
    return shipmentUrl(id) + "/profit";
  }

  // ── helper: create a shipment, return parsed data node ───────────────────

  private JsonNode createShipment(String ref, List<IncomeDTO> incomes, List<CostDTO> costs)
      throws Exception {
    ShipmentDTO dto =
        ShipmentDTO.builder()
            .shipmentRef(ref)
            .description("Integration test — " + ref)
            .shipmentDate(LocalDate.of(2026, 6, 1))
            .incomes(incomes)
            .costs(costs)
            .build();

    ResponseEntity<String> response = restTemplate.postForEntity(shipmentsUrl(), dto, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode root = objectMapper.readTree(response.getBody());
    assertThat(root.get("message").asText()).isNotBlank();
    return root.get("data");
  }

  // ── Test 1: Create + GET ──────────────────────────────────────────────────

  @Test
  void createShipment_andGetById_returnsCorrectData() throws Exception {
    JsonNode created =
        createShipment(
            "SHP-IT-001",
            List.of(IncomeDTO.builder().amount(new BigDecimal("3000")).build()),
            List.of(
                CostDTO.builder()
                    .costTypeName("FUEL")
                    .amount(new BigDecimal("800"))
                    .additionalCost(new BigDecimal("100"))
                    .build()));

    long id = created.get("id").asLong();
    assertThat(id).isPositive();

    ResponseEntity<String> getResp = restTemplate.getForEntity(shipmentUrl(id), String.class);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode data = objectMapper.readTree(getResp.getBody()).get("data");
    assertThat(data.get("shipmentRef").asText()).isEqualTo("SHP-IT-001");
    assertThat(data.get("incomes")).hasSize(1);
    assertThat(data.get("incomes").get(0).get("amount").asDouble()).isEqualTo(3000.0);
    assertThat(data.get("costs")).hasSize(1);
    assertThat(data.get("costs").get(0).get("amount").asDouble()).isEqualTo(800.0);
  }

  // ── Test 2: Profit calculation ────────────────────────────────────────────

  @Test
  void calculateProfit_forNewShipment_returnsCorrectValues() throws Exception {
    // income: 5000 | costs: (1000+200) + (500+0) = 1700 | P/L: +3300
    JsonNode created =
        createShipment(
            "SHP-IT-002",
            List.of(IncomeDTO.builder().amount(new BigDecimal("5000")).build()),
            List.of(
                CostDTO.builder()
                    .costTypeName("FUEL")
                    .amount(new BigDecimal("1000"))
                    .additionalCost(new BigDecimal("200"))
                    .build(),
                CostDTO.builder()
                    .costTypeName("LABOR")
                    .amount(new BigDecimal("500"))
                    .additionalCost(BigDecimal.ZERO)
                    .build()));

    long id = created.get("id").asLong();

    ResponseEntity<String> resp = restTemplate.getForEntity(profitUrl(id), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode data = objectMapper.readTree(resp.getBody()).get("data");
    assertThat(data.get("totalIncome").asDouble()).isEqualTo(5000.0);
    assertThat(data.get("totalCosts").asDouble()).isEqualTo(1700.0);
    assertThat(data.get("profitOrLoss").asDouble()).isEqualTo(3300.0);
    assertThat(data.get("calculatedAt").asText()).isNotBlank();
  }

  // ── Test 3: Get all profit calculations (paginated) ───────────────────────

  @Test
  void getAllProfits_returnsPaginatedPage() throws Exception {
    ResponseEntity<String> resp =
        restTemplate.getForEntity(profitUrl() + "?page=0&size=10", String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode root = objectMapper.readTree(resp.getBody());
    assertThat(root.get("message").asText()).isNotBlank();

    JsonNode page = root.get("data");
    assertThat(page.get("totalElements").asInt()).isGreaterThanOrEqualTo(1);
    assertThat(page.get("content").isArray()).isTrue();

    // verify profit math holds for every item in the page
    for (JsonNode item : page.get("content")) {
      double income = item.get("totalIncome").asDouble();
      double costs = item.get("totalCosts").asDouble();
      double pl = item.get("profitOrLoss").asDouble();
      assertThat(pl).isEqualTo(income - costs);
    }
  }

  // ── Test 4: Delete → 404 ─────────────────────────────────────────────────

  @Test
  void deleteShipment_thenGetById_returns404() throws Exception {
    JsonNode created =
        createShipment(
            "SHP-IT-DELETE",
            List.of(IncomeDTO.builder().amount(new BigDecimal("1000")).build()),
            List.of(
                CostDTO.builder()
                    .costTypeName("TOLL")
                    .amount(new BigDecimal("200"))
                    .additionalCost(BigDecimal.ZERO)
                    .build()));

    long id = created.get("id").asLong();

    ResponseEntity<String> deleteResp =
        restTemplate.exchange(shipmentUrl(id), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
    assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> getResp = restTemplate.getForEntity(shipmentUrl(id), String.class);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    JsonNode error = objectMapper.readTree(getResp.getBody());
    assertThat(error.get("title").asText()).isEqualTo("Resource Not Found");
    assertThat(error.get("detail").asText()).contains(String.valueOf(id));
  }

  // ── Test 5: Update replaces incomes and costs ─────────────────────────────

  @Test
  void updateShipment_replacesIncomesAndCosts() throws Exception {
    JsonNode created =
        createShipment(
            "SHP-IT-UPDATE",
            List.of(IncomeDTO.builder().amount(new BigDecimal("2000")).build()),
            List.of(
                CostDTO.builder()
                    .costTypeName("FUEL")
                    .amount(new BigDecimal("500"))
                    .additionalCost(BigDecimal.ZERO)
                    .build()));

    long id = created.get("id").asLong();

    // Update: new ref, 2 incomes, 2 costs
    ShipmentDTO updateDto =
        ShipmentDTO.builder()
            .shipmentRef("SHP-IT-UPDATE-V2")
            .description("Updated description")
            .shipmentDate(LocalDate.of(2026, 6, 15))
            .incomes(
                List.of(
                    IncomeDTO.builder().amount(new BigDecimal("3000")).build(),
                    IncomeDTO.builder().amount(new BigDecimal("500")).build()))
            .costs(
                List.of(
                    CostDTO.builder()
                        .costTypeName("CUSTOMS")
                        .amount(new BigDecimal("400"))
                        .additionalCost(new BigDecimal("50"))
                        .build(),
                    CostDTO.builder()
                        .costTypeName("STORAGE")
                        .amount(new BigDecimal("150"))
                        .additionalCost(BigDecimal.ZERO)
                        .build()))
            .build();

    ResponseEntity<String> updateResp =
        restTemplate.exchange(
            shipmentUrl(id), HttpMethod.PUT, new HttpEntity<>(updateDto), String.class);
    assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode data = objectMapper.readTree(updateResp.getBody()).get("data");
    assertThat(data.get("shipmentRef").asText()).isEqualTo("SHP-IT-UPDATE-V2");
    assertThat(data.get("description").asText()).isEqualTo("Updated description");
    assertThat(data.get("incomes")).hasSize(2);
    assertThat(data.get("costs")).hasSize(2);

    // verify new profit: income=3500, costs=(400+50)+(150+0)=600, P/L=2900
    ResponseEntity<String> profitResp = restTemplate.getForEntity(profitUrl(id), String.class);
    JsonNode profit = objectMapper.readTree(profitResp.getBody()).get("data");
    assertThat(profit.get("totalIncome").asDouble()).isEqualTo(3500.0);
    assertThat(profit.get("totalCosts").asDouble()).isEqualTo(600.0);
    assertThat(profit.get("profitOrLoss").asDouble()).isEqualTo(2900.0);
  }
}
