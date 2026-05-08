-- ================================================================
-- Seed Data
--   H2-friendly and idempotent: uses MERGE with fixed IDs
-- ================================================================

-- ================================================================
-- COST TYPES
-- ================================================================
MERGE INTO cost_type (cost_type_id, type_name, description) KEY (cost_type_id) VALUES
    (1, 'FUEL', 'Fuel surcharge and transport fuel costs'),
    (2, 'CUSTOMS', 'Customs clearance and duties'),
    (3, 'LABOR', 'Handling and warehouse labor'),
    (4, 'TOLL', 'Road tolls and route charges'),
    (5, 'STORAGE', 'Temporary storage and warehousing');

-- ================================================================
-- SHIPMENTS
-- ================================================================
MERGE INTO shipment (shipment_id, shipment_ref, description, shipment_date, created_at, profitloss) KEY (shipment_id) VALUES
    (101, 'SHP-2026-001', 'Electronics shipment from London to Berlin',          DATE '2026-04-10', CURRENT_TIMESTAMP, -1375.00),
    (102, 'SHP-2026-002', 'Furniture shipment from Manchester to Paris',          DATE '2026-04-12', CURRENT_TIMESTAMP,  2500.00),
    (103, 'SHP-2026-003', 'Pharmaceutical shipment from Birmingham to Amsterdam', DATE '2026-04-15', CURRENT_TIMESTAMP,  4765.00),
    (104, 'SHP-2026-004', 'Retail goods shipment from Leeds to Brussels',         DATE '2026-04-18', CURRENT_TIMESTAMP,  2190.00);

-- ================================================================
-- INCOME
--   Multiple income rows are allowed, but each seed row has a fixed ID
-- ================================================================
MERGE INTO income (income_id, shipment_id, amount, created_at) KEY (income_id) VALUES
    (1001, 101, 1250.00, CURRENT_TIMESTAMP),
    (1002, 102, 4300.00, CURRENT_TIMESTAMP),
    (1003, 103, 6100.00, CURRENT_TIMESTAMP),
    (1004, 104, 3900.00, CURRENT_TIMESTAMP);

-- ================================================================
-- COSTS
--   total_costs in the summary view = SUM(amount + additional_cost)
-- ================================================================
MERGE INTO cost (cost_id, shipment_id, cost_type_id, amount, additional_cost, created_at) KEY (cost_id) VALUES
    (2001, 101, 1, 1200.00, 150.00, CURRENT_TIMESTAMP),
    (2002, 101, 4, 800.00, 25.00, CURRENT_TIMESTAMP),
    (2003, 101, 3, 450.00, 0.00, CURRENT_TIMESTAMP),

    (2004, 102, 1, 900.00, 100.00, CURRENT_TIMESTAMP),
    (2005, 102, 5, 350.00, 50.00, CURRENT_TIMESTAMP),
    (2006, 102, 3, 400.00, 0.00, CURRENT_TIMESTAMP),

    (2007, 103, 1, 10.00, 200.00, CURRENT_TIMESTAMP),
    (2008, 103, 2, 500.00, 75.00, CURRENT_TIMESTAMP),
    (2009, 103, 3, 550.00, 0.00, CURRENT_TIMESTAMP),

    (2010, 104, 1, 850.00, 80.00, CURRENT_TIMESTAMP),
    (2011, 104, 4, 540.00, 20.00, CURRENT_TIMESTAMP),
    (2012, 104, 5, 180.00, 40.00, CURRENT_TIMESTAMP);
