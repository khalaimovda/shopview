document.addEventListener('DOMContentLoaded', () => {
  // Update the total price for a single product row
  function updateProductTotal(cartRow) {
    const quantityElem = cartRow.querySelector('.product-count-regulation-value');
    const quantity = parseInt(quantityElem.textContent.trim());
    const unitPriceElem = cartRow.querySelector('.product-price span');
    const unitPrice = parseFloat(unitPriceElem.textContent.trim());
    const total = quantity * unitPrice;
    const totalElem = cartRow.querySelector('.product-total span');
    if (totalElem) {
      totalElem.textContent = total.toFixed(2);
    }
  }

  // Recalculate overall order total from all product rows
  function updateOrderTotal() {
    let orderTotal = 0;
    const productRows = document.querySelectorAll('.products .product');
    productRows.forEach(row => {
      const totalElem = row.querySelector('.product-total span');
      if (totalElem) {
        orderTotal += parseFloat(totalElem.textContent);
      }
    });
    const orderTotalElem = document.querySelector('.order-total .total span');
    if (orderTotalElem) {
      orderTotalElem.textContent = orderTotal.toFixed(2);
    }
  }

  // Check if the cart is empty; if so, update the main container to display "Ваша корзина пуста"
  function checkIfCartIsEmpty() {
    const productRows = document.querySelectorAll('.products .product');
    if (productRows.length === 0) {
      // Replace the entire content of the main element with the empty cart message
      const mainContainer = document.querySelector('main');
      if (mainContainer) {
        mainContainer.innerHTML = 'Ваша корзина пуста';
      }
    }
  }

  // Increase product quantity in the cart
  function incrementProductQuantity(cartRow) {
    const productId = cartRow.dataset.productId;
    const quantityElem = cartRow.querySelector('.product-count-regulation-value');
    fetch(`/cart/add/${productId}`, { method: 'POST' })
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error increasing product: ${response.statusText}`);
        }
        let quantity = parseInt(quantityElem.textContent.trim());
        quantity++;
        quantityElem.textContent = quantity;
        updateProductTotal(cartRow);
        updateOrderTotal();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Error increasing product quantity');
      });
  }

  // Decrease product quantity; if quantity reaches 0, remove the product row
  function decrementProductQuantity(cartRow) {
    const productId = cartRow.dataset.productId;
    const quantityElem = cartRow.querySelector('.product-count-regulation-value');
    let quantity = parseInt(quantityElem.textContent.trim());
    if (quantity <= 1) {
      removeProductFromCart(cartRow);
    } else {
      fetch(`/cart/decrease/${productId}`, { method: 'POST' })
        .then(response => {
          if (!response.ok) {
            throw new Error(`Error decreasing product: ${response.statusText}`);
          }
          quantity--;
          quantityElem.textContent = quantity;
          updateProductTotal(cartRow);
          updateOrderTotal();
        })
        .catch(error => {
          console.error('Error:', error);
          alert('Error decreasing product quantity');
        });
    }
  }

  // Remove product from the cart completely
  function removeProductFromCart(cartRow) {
    const productId = cartRow.dataset.productId;
    fetch(`/cart/remove/${productId}`, { method: 'DELETE' })
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error removing product: ${response.statusText}`);
        }
        cartRow.remove();
        updateOrderTotal();
        checkIfCartIsEmpty();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Error removing product from cart');
      });
  }

  // Place order (placeholder – adjust as needed for your backend)
  function placeOrder() {
    fetch('/order/create', { method: 'POST' })
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error placing order: ${response.statusText}`);
        }
        alert('Order placed successfully!');
        window.location.reload();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Error placing order');
      });
  }

  // Event listeners for increment buttons
  const incrementButtons = document.querySelectorAll('.product-count-regulation-stepper.increment');
  incrementButtons.forEach(button => {
    button.addEventListener('click', () => {
      const cartRow = button.closest('.product');
      incrementProductQuantity(cartRow);
    });
  });

  // Event listeners for decrement buttons
  const decrementButtons = document.querySelectorAll('.product-count-regulation-stepper.decrement');
  decrementButtons.forEach(button => {
    button.addEventListener('click', () => {
      const cartRow = button.closest('.product');
      decrementProductQuantity(cartRow);
    });
  });

  // Event listeners for remove buttons
  const removeButtons = document.querySelectorAll('.product-count-remove-btn');
  removeButtons.forEach(button => {
    button.addEventListener('click', () => {
      const cartRow = button.closest('.product');
      removeProductFromCart(cartRow);
    });
  });

  // Event listener for the order button
  const orderBtn = document.querySelector('.order-btn');
  if (orderBtn) {
    orderBtn.addEventListener('click', () => {
      placeOrder();
    });
  }

  // Initialize totals on page load
  const productRows = document.querySelectorAll('.products .product');
  productRows.forEach(row => {
    updateProductTotal(row);
  });
  updateOrderTotal();
});