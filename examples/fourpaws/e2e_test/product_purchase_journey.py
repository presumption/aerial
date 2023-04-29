import pytest


@pytest.mark.order(1)
def test_prepare_data():
    customer = create_customer("Alice")
    create_product("FourPaws kitty treats")
    create_random_products()
    customer.login()


# aerial:journey Alice is looking to buy some delicious treats for her cat

@pytest.mark.order(2)
def test_browse():
    # scenario: Customer can search the product catalogue for the type of product they want to buy.
    catalogue.search("cat treats")
    catalogue.select_product("FourPaws kitty treats")


@pytest.mark.order(3)
def test_view_details():
    # scenario: Customer can view photos of a product they are interested in.
    product_photos.scroll_right()
    product_photos.zoom_in()
    product_photos.zoom_out()
    # scenario: Customer can add a product to cart
    add_to_cart()


@pytest.mark.order(4)
def test_purchase():
    # scenario: Customer can check what's in their cart
    go_to_cart()
    # scenario: Customer can change the contents of their cart
    cart.item("FourPaws kitty treats").change_amount(2)
    cart.checkout()
    # scenario: Customer can specify their address during checkout
    shipping.set_address("100 Quality Road")
    shipping.proceed_to_payment()
    # scenario: Customer can pay with credit card
    payment.complete(TestCreditCard())


# aerial:end
