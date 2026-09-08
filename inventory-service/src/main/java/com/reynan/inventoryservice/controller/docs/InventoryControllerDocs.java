package com.reynan.inventoryservice.controller.docs;

import com.reynan.inventoryservice.dto.request.CreateInventoryDTO;
import com.reynan.inventoryservice.dto.response.ResponseInventoryDTO;
import com.reynan.inventoryservice.exceptions.model.StandardError;
import com.reynan.inventoryservice.exceptions.model.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Inventory", description = "Endpoints for creating inventory and managing stock")
public interface InventoryControllerDocs {

    @Operation(
            summary = "Create inventory",
            description = "Creates an inventory record for a product that does not already have one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory created successfully",
                    content = @Content(schema = @Schema(implementation = ResponseInventoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "404", description = "Product was not found",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "409", description = "The product already has an inventory record",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ResponseInventoryDTO> createInventory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product identifier and initial stock quantity",
                    required = true
            )
            CreateInventoryDTO request
    );

    @Operation(
            summary = "Add stock",
            description = "Adds the supplied quantity to an existing inventory record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock added successfully",
                    content = @Content(schema = @Schema(implementation = ResponseInventoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Inventory was not found",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ResponseInventoryDTO> addStock(
            @Parameter(description = "Inventory identifier", required = true, example = "1") Long inventoryId,
            @Parameter(description = "Quantity to add", required = true, example = "10") int quantity
    );

    @Operation(
            summary = "Remove stock",
            description = "Removes the supplied quantity when the inventory has sufficient stock."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock removed successfully",
                    content = @Content(schema = @Schema(implementation = ResponseInventoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "The inventory has insufficient stock",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Inventory was not found",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ResponseInventoryDTO> removeStock(
            @Parameter(description = "Inventory identifier", required = true, example = "1") Long inventoryId,
            @Parameter(description = "Quantity to remove", required = true, example = "5") int quantity
    );
}
