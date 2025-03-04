// Elements
const cart = document.querySelector('.product-purchase-cart');
const increments = document.querySelectorAll('.product-purchase-cart-quantity-stepper.increment');
const decrements = document.querySelectorAll('.product-purchase-cart-quantity-stepper.decrement');
const addBtn = document.querySelector('.product-purchase-cart-add');
const removeBtn = document.querySelector('.product-purchase-cart-remove');
const quantity = document.querySelector('.product-purchase-cart-quantity');
const quantityValue = document.querySelector('.product-purchase-cart-quantity-value');

// Product ID from data attribute
const productId = cart.closest('.product').dataset.productId;

const addProductToCart = () => {
  fetch(`/cart/add/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => {
    addBtn.style.display = 'none';
    removeBtn.style.display = 'flex';
    quantity.style.display = 'flex';
    quantityValue.textContent = '1';
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error adding product to cart. See JS console');
  });
};

const removeProductFromCart = () => {
  fetch(`/cart/remove/${productId}`, {method: 'DELETE'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => {
    addBtn.style.display = 'flex';
    removeBtn.style.display = 'none';
    quantity.style.display = 'none';
    quantityValue.textContent = '0';
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error removing product from cart. See JS console');
  });
};

const incrementProductQuantityInCart = () => {
  fetch(`/cart/add/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => {
    quantityValue.textContent = (parseInt(quantityValue.textContent.trim()) + 1).toString();
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error incrementing product quantity in cart. See JS console');
  });
};

const decrementProductQuantityInCart = () => {
  fetch(`/cart/decrease/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => {
    const currentQuantity = parseInt(quantityValue.textContent.trim());
    quantityValue.textContent = (currentQuantity - 1).toString();

    if (currentQuantity === 1) {
      addBtn.style.display = 'flex';
      removeBtn.style.display = 'none';
      quantity.style.display = 'none';
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error decrementing product quantity in cart. See JS console');
  });
};

// Initial setup for product quantities
const setUpProductQuantity = () => {
  if (parseInt(quantityValue.textContent.trim()) === 0) {
    addBtn.style.display = 'flex';
    removeBtn.style.display = 'none';
    quantity.style.display = 'none';
  } else {
    addBtn.style.display = 'none';
    removeBtn.style.display = 'flex';
    quantity.style.display = 'flex';
  }
};

// Event Listeners
increments.forEach(increment =>
  increment.addEventListener('click', incrementProductQuantityInCart)
);

decrements.forEach(decrement =>
  decrement.addEventListener('click', decrementProductQuantityInCart)
);

addBtn.addEventListener('click', addProductToCart);
removeBtn.addEventListener('click', removeProductFromCart);

// Initial setup on page load
document.addEventListener('DOMContentLoaded', setUpProductQuantity);