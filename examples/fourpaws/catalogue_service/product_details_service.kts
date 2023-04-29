package catalogue_service

import org.junit.jupiter.api.Test

@Test
fun test_GET_product_details() {
    // aerial:example API returns requested product details.
    // scenario: Customer can view the full description of a product they are interested in.
}

@Test
fun test_GET_product_details_with_photos() {
    // aerial:example API response contains links to product photos.
    // scenario: Customer can view photos of a product they are interested in.
}

@Test
fun test_GET_product_details_product_out_of_stock() {
    // aerial:example Out-of-stock status is reflected in the API responses.
    // scenario: Customer can identify which items are out of stock while shopping.
}
