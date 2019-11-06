package com.marketplace.web;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductHalControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductRepository productRepository;

  @Test
  public void testFindAll() throws Exception {

    // Request Page #3 (at index 2) with size 3 and sort on color
    Pageable pageable = PageRequest.of(2, 3, Sort.by(Direction.ASC, "color"));

    // 3 elements returned by repository for Page #3
    List<Product> products = Arrays.asList(
        new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS", BigDecimal.valueOf(10000.0),
            "Blue"),
        new Product(2L, "REP7876543", "Jeans", "Straight fit jeans", "REPLAY",
            BigDecimal.valueOf(15000.0), "Light Blue"),
        new Product(3L, "BOS9987676", "Shirt", "Button Down Oxford", "BOSS",
            BigDecimal.valueOf(12000.0), "White"));

    // 15 Elements over 5 pages, 3 per page cas onfigured in pageable above
    Page<Product> productPage = new PageImpl<Product>(products, pageable, 15);

    when(productRepository.findAll(ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class)))
        .thenReturn(productPage);

    // Execute request
    mockMvc
        .perform(get("/products").param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()))
            .param("sort", String.valueOf(pageable.getSort()))
            .accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(jsonPath("$.PagedResources._embedded.products.length()", is(3)))

        // Assert sorting order for color. Expected : Blue --> Light Blue --> White
        .andExpect(jsonPath("$.PagedResources._embedded.products[0].color", is("Blue")))
        .andExpect(jsonPath("$.PagedResources._embedded.products[1].color", is("Light Blue")))
        .andExpect(jsonPath("$.PagedResources._embedded.products[2].color", is("White")))

        // Assert pagination Links
        .andExpect(jsonPath("$.PagedResources._links.first.href",
            is("http://localhost/products?page=0&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.prev.href",
            is("http://localhost/products?page=1&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.self.href",
            is("http://localhost/products?page=2&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.next.href",
            is("http://localhost/products?page=3&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.last.href",
            is("http://localhost/products?page=4&size=3&sort=color,asc")))

        // Assert page metadata
        .andExpect(jsonPath("$.PagedResources.page.size", is(3)))
        .andExpect(jsonPath("$.PagedResources.page.totalElements", is(15)))
        .andExpect(jsonPath("$.PagedResources.page.totalPages", is(5)))
        .andExpect(jsonPath("$.PagedResources.page.number", is(2)))

        .andReturn();
  }

  @Test
  public void testFindById() throws Exception {

    when(productRepository.findById(1L)).thenReturn(Optional.of(new Product(1L, "GAS1234567",
        "Jeans", "Slim fit jeans", "GAS", BigDecimal.valueOf(15000.0), "Blue")));

    // Execute request
    mockMvc.perform(get("/products/1").accept(MarketPlaceMediaTypes.V1_HAL_UTF8)).andDo(print())
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))

        // Assert product fields
        .andExpect(jsonPath("$.ProductResource.productId", is("GAS1234567")))
        .andExpect(jsonPath("$.ProductResource.title", is("Jeans")))
        .andExpect(jsonPath("$.ProductResource.description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.ProductResource.brand", is("GAS")))
        .andExpect(jsonPath("$.ProductResource.price", is(15000.0)))
        .andExpect(jsonPath("$.ProductResource.color", is("Blue")))

        // Assert Resource links
        .andExpect(
            jsonPath("$.ProductResource._links.self.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.self.type", is("GET")))

        .andExpect(
            jsonPath("$.ProductResource._links.create.href", is("http://localhost/products")))
        .andExpect(jsonPath("$.ProductResource._links.create.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.createBatch.href",
            is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.ProductResource._links.createBatch.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.type", is("PUT")))

        .andExpect(
            jsonPath("$.ProductResource._links.update.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.update.type", is("PATCH")))

        .andExpect(
            jsonPath("$.ProductResource._links.delete.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.delete.type", is("DELETE")))

        .andReturn();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindByTitle() throws Exception {

    // Request Page #2 (at index 1) with size 3 and sort on color
    Pageable pageable = PageRequest.of(1, 3, Sort.by(Direction.ASC, "color"));

    // 3 elements returned by repository for Page #1
    List<Product> products = Arrays.asList(
        new Product(3L, "BOS9987676", "Jeans", "Relaxed fit jeans", "BOSS",
            BigDecimal.valueOf(12000.0), "Black"),
        new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS", BigDecimal.valueOf(15000.0),
            "Blue"),
        new Product(2L, "REP7876543", "Jeans", "Straight fit jeans", "REPLAY",
            BigDecimal.valueOf(15000.0), "Light Blue"));

    // 8 Elements over 3 pages, 3 per page as configured in pageable above
    Page<Product> productPage = new PageImpl<Product>(products, pageable, 8);

    when(productRepository.findAll(ArgumentMatchers.any(Specification.class),
        ArgumentMatchers.any(Pageable.class))).thenReturn(productPage);

    // Execute request
    mockMvc
        .perform(get("/products").param("title", "jeans")
            .param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()))
            .param("sort", String.valueOf(pageable.getSort()))
            .accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(jsonPath("$.PagedResources._embedded.products.length()", is(3)))

        // Assert sorting order for color. Expected : Black -> Blue -> Light Blue
        .andExpect(jsonPath("$.PagedResources._embedded.products[0].color", is("Black")))
        .andExpect(jsonPath("$.PagedResources._embedded.products[1].color", is("Blue")))
        .andExpect(jsonPath("$.PagedResources._embedded.products[2].color", is("Light Blue")))

        // Assert pagination Links
        .andExpect(jsonPath("$.PagedResources._links.first.href",
            is("http://localhost/products?page=0&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.prev.href",
            is("http://localhost/products?page=0&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.self.href",
            is("http://localhost/products?page=1&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.next.href",
            is("http://localhost/products?page=2&size=3&sort=color,asc")))
        .andExpect(jsonPath("$.PagedResources._links.last.href",
            is("http://localhost/products?page=2&size=3&sort=color,asc")))

        // Assert page metadata
        .andExpect(jsonPath("$.PagedResources.page.size", is(3)))
        .andExpect(jsonPath("$.PagedResources.page.totalElements", is(8)))
        .andExpect(jsonPath("$.PagedResources.page.totalPages", is(3)))
        .andExpect(jsonPath("$.PagedResources.page.number", is(1)))

        .andReturn();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindByDescription() throws Exception {

    // Request Page #2 (at index 1) with size 3 and sort on description
    Pageable pageable = PageRequest.of(1, 3, Sort.by(Direction.ASC, "description"));

    // 3 elements returned by repository for Page #1
    List<Product> products = Arrays.asList(
        new Product(3L, "BOS9987676", "Jeans", "Relaxed fit jeans", "BOSS",
            BigDecimal.valueOf(12000.0), "Black"),
        new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS", BigDecimal.valueOf(15000.0),
            "Blue"),
        new Product(2L, "REP7876543", "Jeans", "Straight fit jeans", "REPLAY",
            BigDecimal.valueOf(15000.0), "Light Blue"));

    // 8 Elements over 3 pages, 3 per page as configured in pageable above
    Page<Product> productPage = new PageImpl<Product>(products, pageable, 8);

    when(productRepository.findAll(ArgumentMatchers.any(Specification.class),
        ArgumentMatchers.any(Pageable.class))).thenReturn(productPage);

    // Execute request
    mockMvc
        .perform(get("/products").param("description", "jeans")
            .param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()))
            .param("sort", String.valueOf(pageable.getSort()))
            .accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(jsonPath("$.PagedResources._embedded.products.length()", is(3)))

        // Assert sorting order for description. Expected : Relaxed fit jeans -> Slim fit jeans ->
        // Straight fit jeans
        .andExpect(
            jsonPath("$.PagedResources._embedded.products[0].description", is("Relaxed fit jeans")))
        .andExpect(
            jsonPath("$.PagedResources._embedded.products[1].description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.PagedResources._embedded.products[2].description",
            is("Straight fit jeans")))

        // Assert pagination Links
        .andExpect(jsonPath("$.PagedResources._links.first.href",
            is("http://localhost/products?page=0&size=3&sort=description,asc")))
        .andExpect(jsonPath("$.PagedResources._links.prev.href",
            is("http://localhost/products?page=0&size=3&sort=description,asc")))
        .andExpect(jsonPath("$.PagedResources._links.self.href",
            is("http://localhost/products?page=1&size=3&sort=description,asc")))
        .andExpect(jsonPath("$.PagedResources._links.next.href",
            is("http://localhost/products?page=2&size=3&sort=description,asc")))
        .andExpect(jsonPath("$.PagedResources._links.last.href",
            is("http://localhost/products?page=2&size=3&sort=description,asc")))

        // Assert page metadata
        .andExpect(jsonPath("$.PagedResources.page.size", is(3)))
        .andExpect(jsonPath("$.PagedResources.page.totalElements", is(8)))
        .andExpect(jsonPath("$.PagedResources.page.totalPages", is(3)))
        .andExpect(jsonPath("$.PagedResources.page.number", is(1)))

        .andReturn();
  }

  @Test
  public void testNewProduct() throws Exception {

    Product product = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(15000.0), "Blue");

    when(productRepository.existsByProductId("GAS1234567")).thenReturn(false);
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(product);

    mockMvc
        .perform(post("/products").content(objectMapper.writeValueAsString(product))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))


        // Assert product fields

        .andExpect(jsonPath("$.ProductResource.productId", is("GAS1234567")))
        .andExpect(jsonPath("$.ProductResource.title", is("Jeans")))
        .andExpect(jsonPath("$.ProductResource.description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.ProductResource.brand", is("GAS")))
        .andExpect(jsonPath("$.ProductResource.price", is(15000.0)))
        .andExpect(jsonPath("$.ProductResource.color", is("Blue")))

        // Assert Resource links

        .andExpect(
            jsonPath("$.ProductResource._links.self.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.self.type", is("GET")))

        .andExpect(
            jsonPath("$.ProductResource._links.create.href", is("http://localhost/products")))
        .andExpect(jsonPath("$.ProductResource._links.create.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.createBatch.href",
            is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.ProductResource._links.createBatch.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.type", is("PUT")))

        .andExpect(
            jsonPath("$.ProductResource._links.update.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.update.type", is("PATCH")))

        .andExpect(
            jsonPath("$.ProductResource._links.delete.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.delete.type", is("DELETE")))

        // Assert Location Header
        .andExpect(header().string("Location", "http://localhost/products/1"))

        .andReturn();
  }

  @Test
  public void testNewProductExisting() throws Exception {

    Product product = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(15000.0), "Blue");

    when(productRepository.existsByProductId("GAS1234567")).thenReturn(true);
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(product);

    mockMvc
        .perform(post("/products").content(objectMapper.writeValueAsString(product))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isConflict())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(content().string(
            "Product with Id : GAS1234567 already exists. Please send a PUT/PATCH request to update this product"))

        .andReturn();
  }

  @Test
  public void testNewProductBatchSuccess() throws Exception {

    Product product1 = new Product(1L, "ARMBLT101", "Belt", "Reversible genuine leather belt",
        "Armani", BigDecimal.valueOf(3500.0), "Black/Brown");
    Product product2 =
        new Product(2L, "COALTHBG102", "Cardigan", "Cashmere cardigan with henley collar",
            "Ralph Lauren", BigDecimal.valueOf(9000.0), "Charcoal Black");

    ProductBatch<Product> productBatch = new ProductBatch<>(Arrays.asList(product1, product2));

    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(product1,
        product2);

    mockMvc
        .perform(post("/products/batch").content(objectMapper.writeValueAsString(productBatch))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isMultiStatus())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))

        // Assert Batch Status fields
        .andExpect(
            jsonPath("$.Resource.products[0].httpStatus", is(HttpStatus.OK.getReasonPhrase())))
        .andExpect(jsonPath("$.Resource.products[0].uri", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].httpMethod", is("POST")))

        // Assert product fields

        .andExpect(jsonPath("$.Resource.products[0].product.productId", is("ARMBLT101")))
        .andExpect(jsonPath("$.Resource.products[0].product.title", is("Belt")))
        .andExpect(jsonPath("$.Resource.products[0].product.description",
            is("Reversible genuine leather belt")))
        .andExpect(jsonPath("$.Resource.products[0].product.brand", is("Armani")))
        .andExpect(jsonPath("$.Resource.products[0].product.price", is(3500.0)))
        .andExpect(jsonPath("$.Resource.products[0].product.color", is("Black/Brown")))

        // Assert Resource links

        .andExpect(jsonPath("$.Resource.products[0].product._links.self.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.self.type", is("GET")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.create.href",
            is("http://localhost/products")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.create.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.createBatch.href",
            is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.createBatch.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.updateOrCreate.type", is("PUT")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.update.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.update.type", is("PATCH")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.delete.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.delete.type", is("DELETE")))

        // Repeat for second product

        // Assert Batch Status fields
        .andExpect(
            jsonPath("$.Resource.products[1].httpStatus", is(HttpStatus.OK.getReasonPhrase())))
        .andExpect(jsonPath("$.Resource.products[1].uri", is("http://localhost/products/2")))
        .andExpect(jsonPath("$.Resource.products[1].httpMethod", is("POST")))

        // Assert product fields

        .andExpect(jsonPath("$.Resource.products[1].product.productId", is("COALTHBG102")))
        .andExpect(jsonPath("$.Resource.products[1].product.title", is("Cardigan")))
        .andExpect(jsonPath("$.Resource.products[1].product.description",
            is("Cashmere cardigan with henley collar")))
        .andExpect(jsonPath("$.Resource.products[1].product.brand", is("Ralph Lauren")))
        .andExpect(jsonPath("$.Resource.products[1].product.price", is(9000.0)))
        .andExpect(jsonPath("$.Resource.products[1].product.color", is("Charcoal Black")))

        // Assert Resource links

        .andExpect(jsonPath("$.Resource.products[1].product._links.self.href",
            is("http://localhost/products/2")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.self.type", is("GET")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.create.href",
            is("http://localhost/products")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.create.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[1].product._links.createBatch.href",
            is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.createBatch.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[1].product._links.updateOrCreate.href",
            is("http://localhost/products/2")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.updateOrCreate.type", is("PUT")))

        .andExpect(jsonPath("$.Resource.products[1].product._links.update.href",
            is("http://localhost/products/2")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.update.type", is("PATCH")))

        .andExpect(jsonPath("$.Resource.products[1].product._links.delete.href",
            is("http://localhost/products/2")))
        .andExpect(jsonPath("$.Resource.products[1].product._links.delete.type", is("DELETE")))

        // Assert the batch resource link

        .andExpect(jsonPath("$.Resource._links.self.href", is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.Resource._links.self.type", is("POST")))

        .andReturn();
  }

  @Test
  public void testNewProductBatchSuccessAndFail() throws Exception {

    Product product1 = new Product("ARMBLT101", "Belt", "Reversible genuine leather belt", "Armani",
        BigDecimal.valueOf(3500.0), "Black/Brown");
    
    Product product2 =
        new Product("COALTHBG102", "Cardigan", "Cashmere cardigan with henley collar",
            "Ralph Lauren", BigDecimal.valueOf(9000.0), "Charcoal Black");

    // Should Give a conflict for duplicate productId - ARMBLT101
    Product product3 = new Product("ARMBLT101", "Belt", "Reversible genuine leather belt", "Armani",
        BigDecimal.valueOf(3500.0), "Black/Brown");

    Product savedProduct1 = new Product(1L, "ARMBLT101", "Belt", "Reversible genuine leather belt",
        "Armani", BigDecimal.valueOf(3500.0), "Black/Brown");

    ProductBatch<Product> productBatch =
        new ProductBatch<>(Arrays.asList(product1, product2, product3));

    when(productRepository.existsByProductId("ARMBLT101")).thenReturn(false).thenReturn(true);
    when(productRepository.existsByProductId("COALTHBG102")).thenReturn(false);
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(savedProduct1)
        .thenThrow(new RuntimeException("Unable to save product"));

    // Remove "\"price\":9000.0," from JSON for product 2 to simulate missed attribute

    mockMvc
        .perform(post("/products/batch")
            .content(objectMapper.writeValueAsString(productBatch).replace("\"price\":9000.0,", ""))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isMultiStatus())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))

        // Assert Batch Status fields
        .andExpect(
            jsonPath("$.Resource.products[0].httpStatus", is(HttpStatus.OK.getReasonPhrase())))
        .andExpect(jsonPath("$.Resource.products[0].uri", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].httpMethod", is("POST")))

        // Assert product fields

        .andExpect(jsonPath("$.Resource.products[0].product.productId", is("ARMBLT101")))
        .andExpect(jsonPath("$.Resource.products[0].product.title", is("Belt")))
        .andExpect(jsonPath("$.Resource.products[0].product.description",
            is("Reversible genuine leather belt")))
        .andExpect(jsonPath("$.Resource.products[0].product.brand", is("Armani")))
        .andExpect(jsonPath("$.Resource.products[0].product.price", is(3500.0)))
        .andExpect(jsonPath("$.Resource.products[0].product.color", is("Black/Brown")))

        // Assert Resource links

        .andExpect(jsonPath("$.Resource.products[0].product._links.self.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.self.type", is("GET")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.create.href",
            is("http://localhost/products")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.create.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.createBatch.href",
            is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.createBatch.type", is("POST")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.updateOrCreate.type", is("PUT")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.update.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.update.type", is("PATCH")))

        .andExpect(jsonPath("$.Resource.products[0].product._links.delete.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.Resource.products[0].product._links.delete.type", is("DELETE")))

        // Repeat for second product - Bad Request

        // Assert Batch Status fields
        .andExpect(jsonPath("$.Resource.products[1].httpStatus", is("BAD_REQUEST")))
        .andExpect(jsonPath("$.Resource.products[1].error", is("Unable to save product")))
        .andExpect(jsonPath("$.Resource.products[1].httpMethod", is("POST")))

        // Assert product fields

        .andExpect(jsonPath("$.Resource.products[1].product.productId", is("COALTHBG102")))
        .andExpect(jsonPath("$.Resource.products[1].product.title", is("Cardigan")))
        .andExpect(jsonPath("$.Resource.products[1].product.description",
            is("Cashmere cardigan with henley collar")))
        .andExpect(jsonPath("$.Resource.products[1].product.brand", is("Ralph Lauren")))
        .andExpect(jsonPath("$.Resource.products[1].product.price", IsNull.nullValue()))
        .andExpect(jsonPath("$.Resource.products[1].product.color", is("Charcoal Black")))

        // Repeat for third product - Conflict

        // Assert Batch Status fields
        .andExpect(jsonPath("$.Resource.products[2].httpStatus", is("CONFLICT")))
        .andExpect(jsonPath("$.Resource.products[2].error", is(
            "Product with Id : ARMBLT101 already exists. Please send a PUT/PATCH request to update this product")))
        .andExpect(jsonPath("$.Resource.products[2].httpMethod", is("POST")))

        // Assert product fields

        .andExpect(jsonPath("$.Resource.products[2].product.productId", is("ARMBLT101")))
        .andExpect(jsonPath("$.Resource.products[2].product.title", is("Belt")))
        .andExpect(jsonPath("$.Resource.products[2].product.description",
            is("Reversible genuine leather belt")))
        .andExpect(jsonPath("$.Resource.products[2].product.brand", is("Armani")))
        .andExpect(jsonPath("$.Resource.products[2].product.price", is(3500.0)))
        .andExpect(jsonPath("$.Resource.products[2].product.color", is("Black/Brown")))


        // Assert the batch resource link

        .andExpect(jsonPath("$.Resource._links.self.href", is("http://localhost/products/batch")))
        .andExpect(jsonPath("$.Resource._links.self.type", is("POST")))

        .andReturn();
  }

  @Test
  public void testUpdateProductPutForUpdate() throws Exception {

    Product existingproduct = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(15000.0), "Blue");
    Product updatedproduct = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(12000.0), "Black");

    when(productRepository.findById(ArgumentMatchers.any(Long.class)))
        .thenReturn(Optional.of(existingproduct));
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(updatedproduct);

    mockMvc
        .perform(put("/products/1").content(objectMapper.writeValueAsString(updatedproduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))


        // Assert product fields

        .andExpect(jsonPath("$.ProductResource.productId", is("GAS1234567")))
        .andExpect(jsonPath("$.ProductResource.title", is("Jeans")))
        .andExpect(jsonPath("$.ProductResource.description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.ProductResource.brand", is("GAS")))
        .andExpect(jsonPath("$.ProductResource.price", is(12000.0)))
        .andExpect(jsonPath("$.ProductResource.color", is("Black")))

        // Assert Resource links

        .andExpect(
            jsonPath("$.ProductResource._links.self.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.self.type", is("GET")))

        .andExpect(
            jsonPath("$.ProductResource._links.create.href", is("http://localhost/products")))
        .andExpect(jsonPath("$.ProductResource._links.create.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.type", is("PUT")))

        .andExpect(
            jsonPath("$.ProductResource._links.update.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.update.type", is("PATCH")))

        .andExpect(
            jsonPath("$.ProductResource._links.delete.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.delete.type", is("DELETE")))

        .andReturn();
  }


  @Test
  public void testUpdateProductPutForCreate() throws Exception {

    Product updatedproduct = new Product(2L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(12000.0), "Black");

    when(productRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.empty());
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(updatedproduct);

    mockMvc
        .perform(put("/products/2").content(objectMapper.writeValueAsString(updatedproduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))


        // Assert product fields

        .andExpect(jsonPath("$.ProductResource.productId", is("GAS1234567")))
        .andExpect(jsonPath("$.ProductResource.title", is("Jeans")))
        .andExpect(jsonPath("$.ProductResource.description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.ProductResource.brand", is("GAS")))
        .andExpect(jsonPath("$.ProductResource.price", is(12000.0)))
        .andExpect(jsonPath("$.ProductResource.color", is("Black")))

        // Assert Resource links

        .andExpect(
            jsonPath("$.ProductResource._links.self.href", is("http://localhost/products/2")))
        .andExpect(jsonPath("$.ProductResource._links.self.type", is("GET")))

        .andExpect(
            jsonPath("$.ProductResource._links.create.href", is("http://localhost/products")))
        .andExpect(jsonPath("$.ProductResource._links.create.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.href",
            is("http://localhost/products/2")))
        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.type", is("PUT")))

        .andExpect(
            jsonPath("$.ProductResource._links.update.href", is("http://localhost/products/2")))
        .andExpect(jsonPath("$.ProductResource._links.update.type", is("PATCH")))

        .andExpect(
            jsonPath("$.ProductResource._links.delete.href", is("http://localhost/products/2")))
        .andExpect(jsonPath("$.ProductResource._links.delete.type", is("DELETE")))

        // Assert Location Header
        .andExpect(header().string("Location", "http://localhost/products/2"))

        .andReturn();
  }

  @Test
  public void testUpdateProductPatch() throws Exception {

    Product existingproduct = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(15000.0), "Blue");
    Product updatedproduct = new Product();
    updatedproduct.setColor("Beige");
    updatedproduct.setPrice(BigDecimal.valueOf(7000.0));

    when(productRepository.findById(ArgumentMatchers.any(Long.class)))
        .thenReturn(Optional.of(existingproduct));
    when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(updatedproduct);

    mockMvc
        .perform(patch("/products/1").content(objectMapper.writeValueAsString(updatedproduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))


        // Assert product fields

        .andExpect(jsonPath("$.ProductResource.productId", is("GAS1234567")))
        .andExpect(jsonPath("$.ProductResource.title", is("Jeans")))
        .andExpect(jsonPath("$.ProductResource.description", is("Slim fit jeans")))
        .andExpect(jsonPath("$.ProductResource.brand", is("GAS")))
        .andExpect(jsonPath("$.ProductResource.price", is(7000.0)))
        .andExpect(jsonPath("$.ProductResource.color", is("Beige")))

        // Assert Resource links

        .andExpect(
            jsonPath("$.ProductResource._links.self.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.self.type", is("GET")))

        .andExpect(
            jsonPath("$.ProductResource._links.create.href", is("http://localhost/products")))
        .andExpect(jsonPath("$.ProductResource._links.create.type", is("POST")))

        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.href",
            is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.updateOrCreate.type", is("PUT")))

        .andExpect(
            jsonPath("$.ProductResource._links.update.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.update.type", is("PATCH")))

        .andExpect(
            jsonPath("$.ProductResource._links.delete.href", is("http://localhost/products/1")))
        .andExpect(jsonPath("$.ProductResource._links.delete.type", is("DELETE")))

        .andReturn();
  }

  @Test
  public void testDeleteProduct() throws Exception {

    Product existingproduct = new Product(1L, "GAS1234567", "Jeans", "Slim fit jeans", "GAS",
        BigDecimal.valueOf(15000.0), "Blue");

    when(productRepository.findById(ArgumentMatchers.any(Long.class)))
        .thenReturn(Optional.of(existingproduct));

    mockMvc.perform(delete("/products/1").accept(MarketPlaceMediaTypes.V1_HAL_UTF8)).andDo(print())
        .andExpect(status().isOk()).andReturn();
  }


  // Not Found Scenarios for finders, patch and delete

  @Test
  public void testFindByIdNotFound() throws Exception {

    // Return a 404 since the URL represents a resource

    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    // Execute request
    mockMvc.perform(get("/products/1").accept(MarketPlaceMediaTypes.V1_HAL_UTF8)).andDo(print())
        .andExpect(status().isNotFound());

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindByTitleNotFound() throws Exception {

    // Return a 200 with empty collection since the finder method represents a collection

    Pageable pageable = PageRequest.of(1, 5, Sort.by(Direction.ASC, "color"));

    List<Product> products = Collections.emptyList();

    Page<Product> productPage = new PageImpl<Product>(products, pageable, 0);

    when(productRepository.findAll(ArgumentMatchers.any(Specification.class),
        ArgumentMatchers.any(Pageable.class))).thenReturn(productPage);

    // Execute request
    mockMvc
        .perform(get("/products").param("title", "hats")
            .param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()))
            .param("sort", String.valueOf(pageable.getSort()))
            .accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(jsonPath("$.PagedResources._embedded.products", IsEmptyCollection.empty()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindByDescriptionNotFound() throws Exception {

    // Return a 200 with empty collection since the finder method represents a collection

    Pageable pageable = PageRequest.of(1, 3, Sort.by(Direction.ASC, "color"));

    List<Product> products = Collections.emptyList();

    Page<Product> productPage = new PageImpl<Product>(products, pageable, 0);

    when(productRepository.findAll(ArgumentMatchers.any(Specification.class),
        ArgumentMatchers.any(Pageable.class))).thenReturn(productPage);

    // Execute request
    mockMvc
        .perform(get("/products").param("description", "Regular fit shirts")
            .param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()))
            .param("sort", String.valueOf(pageable.getSort()))
            .accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andExpect(jsonPath("$.PagedResources._embedded.products", IsEmptyCollection.empty()));
  }

  @Test
  public void testUpdateProductPatchNotFound() throws Exception {

    Product updatedproduct = new Product();
    updatedproduct.setColor("Beige");
    updatedproduct.setPrice(BigDecimal.valueOf(7000.0));

    when(productRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(patch("/products/1").content(objectMapper.writeValueAsString(updatedproduct))
            .contentType(MediaType.APPLICATION_JSON).accept(MarketPlaceMediaTypes.V1_HAL_UTF8))
        .andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  @Test
  public void testDeleteProductPatchNotFound() throws Exception {

    when(productRepository.findById(ArgumentMatchers.any(Long.class))).thenReturn(Optional.empty());

    mockMvc.perform(delete("/products/1").accept(MarketPlaceMediaTypes.V1_HAL_UTF8)).andDo(print())
        .andExpect(status().isNotFound()).andReturn();
  }

}
