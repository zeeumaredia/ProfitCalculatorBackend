package com.profitcalculator.dascher.controller;

import com.profitcalculator.dascher.dto.MessageResponse;
import com.profitcalculator.dascher.dto.ProfitDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import com.profitcalculator.dascher.service.ProfitCalculationService;
import com.profitcalculator.dascher.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Profit & Shipment Management",
    description =
        "Manage shipments and calculate profit/loss. "
            + "Each shipment holds its own incomes and costs. "
            + "Test data is pre-loaded with IDs 101 to 104.")
// @Validated on the class activates method-level validation — without it, @Min(1) on path variable
// IDs is silently ignored by Spring MVC.
@Validated
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

  private final ShipmentService shipmentService;
  private final ProfitCalculationService profitCalculationService;
  private final MessageSource messageSource;

  // ── Shipment endpoints ────────────────────────────────────────────────────

  @Operation(
      summary = "Get all shipments",
      description =
          "Returns paginated shipments. " + "Param: page & size. Defaults to page=0, size=10.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<MessageResponse<Page<ShipmentDTO>>> findAllShipments(
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        MessageResponse.of(
            getMsg("shipment.msg.all"), shipmentService.findAll(PageRequest.of(page, size))));
  }

  @Operation(
      summary = "Get shipment by ID",
      description = "Fetches a single shipment by ID. Seeded IDs: 101, 102, 103, 104.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Shipment not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<MessageResponse<ShipmentDTO>> findShipmentById(
      @Parameter(description = "Shipment ID — must be ≥ 1") @PathVariable("id") @Min(1) Long id) {
    return ResponseEntity.ok(
        MessageResponse.of(getMsg("shipment.msg.one"), shipmentService.findById(id)));
  }

  @Operation(
      summary = "Create a shipment",
      description =
          "Creates a new shipment with its incomes and costs. "
              + "shipmentRef must be unique. "
              + "Valid cost types: FUEL, CUSTOMS, LABOR, TOLL, STORAGE.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created successfully"),
    @ApiResponse(responseCode = "400", description = "Validation failed"),
    @ApiResponse(responseCode = "404", description = "Invalid cost type"),
    @ApiResponse(responseCode = "409", description = "shipmentRef already exists"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping
  public ResponseEntity<MessageResponse<ShipmentDTO>> createShipment(
      @Parameter(description = "Shipment body with incomes and costs") @Valid @RequestBody
          ShipmentDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(MessageResponse.of(getMsg("shipment.msg.created"), shipmentService.create(dto)));
  }

  @Operation(
      summary = "Update a shipment",
      description =
          "Replaces shipment fields and all its incomes and costs. "
              + "Old records are cleared and new ones are saved.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request body"),
    @ApiResponse(responseCode = "404", description = "Shipment or cost type not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<MessageResponse<ShipmentDTO>> updateShipment(
      @Parameter(description = "ID of the shipment to update") @PathVariable("id") @Min(1) Long id,
      @Parameter(description = "Full updated shipment with incomes and costs") @Valid @RequestBody
          ShipmentDTO dto) {
    return ResponseEntity.ok(
        MessageResponse.of(getMsg("shipment.msg.updated"), shipmentService.update(id, dto)));
  }

  @Operation(
      summary = "Delete a shipment",
      description = "Deletes the shipment and all its incomes and costs (cascaded).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Shipment not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<MessageResponse<Void>> deleteShipment(
      @Parameter(description = "ID of the shipment to delete") @PathVariable("id") @Min(1)
          Long id) {
    shipmentService.delete(id);
    return ResponseEntity.ok(MessageResponse.of(getMsg("shipment.msg.deleted")));
  }

  // ── Profit endpoints ──────────────────────────────────────────────────────

  @Operation(
      summary = "Get profit summary for all shipments",
      description =
          "Returns paginated profit/loss for every shipment. "
              + "Formula: profitOrLoss = totalIncome − totalCosts. "
              + "Negative value means a loss.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/profit")
  public ResponseEntity<MessageResponse<Page<ProfitDTO>>> findAllProfits(
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        MessageResponse.of(
            getMsg("profit.msg.all"),
            profitCalculationService.findAll(PageRequest.of(page, size))));
  }

  @Operation(
      summary = "Get profit for a shipment",
      description =
          "Calculates live profit/loss for the given shipment ID. "
              + "Data is never cached — always fresh. Try IDs 101 to 104.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Calculated successfully"),
    @ApiResponse(responseCode = "404", description = "Shipment not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/profit")
  public ResponseEntity<MessageResponse<ProfitDTO>> getProfit(
      @Parameter(description = "Shipment ID — try 101, 102, 103 or 104") @PathVariable("id") @Min(1)
          Long id) {
    return ResponseEntity.ok(
        MessageResponse.of(
            getMsg("profit.msg.calculated"), profitCalculationService.calculate(id)));
  }

  // ── private helpers ───────────────────────────────────────────────────────

  private String getMsg(String key) {
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }
}
