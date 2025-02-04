package com.project.shopapp.controllers;

import com.project.shopapp.dtos.*;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.services.IProductService;
import com.project.shopapp.utils.product.EmptyFileUtil;
import com.project.shopapp.utils.product.EmptyFilesUtil;
import com.project.shopapp.utils.product.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final ProductRepository productRepository;
    private final EmptyFileUtil emptyFileUtil;
    private final EmptyFilesUtil emptyFilesUtil;
    private final ImageUtil imageUtil;
    @PostMapping("")
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Product newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping(value = "/uploads",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> uploadImages(
            @RequestParam("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            emptyFilesUtil.checkFile(files);
            Product existingProduct = productService.getProductById(productId);
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                ResponseEntity<?> checkFileRes = emptyFileUtil.checkFile(file);
                if (checkFileRes != null) {
                    return checkFileRes;
                }

                String filename = imageUtil.storeImage(file);
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }
            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<String> getProducts(
            @RequestParam("page")     int page,
            @RequestParam("limit")    int limit
    ) {
        return ResponseEntity.ok("getProducts here");
    }
    //http://localhost:8088/api/v1/products/6
    @GetMapping("/{id}")
    public Product getProductById(
            @PathVariable("id") String productId
    ) throws Exception {
        Product res = productService.existedById(Long.valueOf(productId));

        return res;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable long id) {
        try {
            return productService.deleteProduct(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
