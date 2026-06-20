ALTER TABLE `purchase_plan_items`
    DROP FOREIGN KEY IF EXISTS `FK_purchase_plan_items_ticket_id_purchase_request_tickets`;

ALTER TABLE `purchase_plan_items`
    ADD CONSTRAINT `FK_purchase_plan_items_ticket_id_tickets`
        FOREIGN KEY (`ticket_id`)
        REFERENCES `tickets` (`ticket_id`);
