package com.reynan.inventoryservice.controller.docs;

import com.reynan.inventoryservice.dto.request.CreateProductDTO;
import com.reynan.inventoryservice.dto.response.ResponseProductDTO;
import com.reynan.inventoryservice.exceptions.model.StandardError;
import com.reynan.inventoryservice.exceptions.model.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Products", description = "Endpoints for managing the product catalog")
public interface ProductControllerDocs {

    @Operation(
            summary = "Create a product",
            description = "Creates a product when no other product has the same name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ResponseProductDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "409", description = "A product with this name already exists",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ResponseProductDTO> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Name and price for the product", required = true
            )
            CreateProductDTO request
    );

    @Operation(
            summary = "List products",
            description = "Returns products in pages. Use page, size and sort query parameters to control pagination."
    )
    @ApiResponse(responseCode = "200", description = "Page of products returned successfully")
    ResponseEntity<Page<ResponseProductDTO>> findAllProducts(@ParameterObject Pageable pageable);

    @Operation(summary = "Find a product by name", description = "Returns the product whose name exactly matches the supplied value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ResponseProductDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product was not found",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ResponseProductDTO> findByName(
            @Parameter(description = "Exact product name", required = true, example = "Notebook") String name
    );

    @Operation(summary = "Delete a product", description = "Permanently removes the product identified by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product was not found",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product identifier", required = true, example = "1") Long id
    );
}
