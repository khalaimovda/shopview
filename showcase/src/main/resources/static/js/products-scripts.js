const defaultTextFilterInputValue = '';
const defaultPageSizeValue = 10;

// Elements
const modalOverlay = document.getElementById('modalOverlay');
const textFilterInput = document.getElementById('productTextFilterInput');
const productCreateBtn = document.getElementById('productCreateBtn');
const form = document.getElementById('form');
const increments = document.querySelectorAll('.product-purchase-cart-quantity-stepper.increment');
const decrements = document.querySelectorAll('.product-purchase-cart-quantity-stepper.decrement');
const adds = document.querySelectorAll('.product-purchase-cart-add');
const removals = document.querySelectorAll('.product-purchase-cart-remove');
const carts = document.querySelectorAll('.product-purchase-cart');
const pageSize = document.getElementById('pageSize');

// Functions
const openModal = () => {
  modalOverlay.style.display = 'flex';
  document.body.classList.add('modal-open'); // block scroll
};

const closeModal = () => {
  modalOverlay.style.display = 'none';
  document.body.classList.remove('modal-open'); // unblock scroll
  form.reset();
};


const getProductsWithTextFilter = (search) => {
    const url = new URL(window.location.href);
    url.searchParams.set('search', search);
    url.searchParams.set('page', 0); // Request first page if we changed search param
    window.location.href = url.toString();
};


const addProductToCart = (cart) => {
  const addBtn = cart.querySelector('.product-purchase-cart-add');
  const removeBtn = cart.querySelector('.product-purchase-cart-remove');
  const quantity = cart.querySelector('.product-purchase-cart-quantity');
  const quantityValue = cart.querySelector('.product-purchase-cart-quantity-value');

  const productId = cart.dataset.productId;

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

const removeProductFromCart = (cart) => {
  const addBtn = cart.querySelector('.product-purchase-cart-add');
  const removeBtn = cart.querySelector('.product-purchase-cart-remove');
  const quantity = cart.querySelector('.product-purchase-cart-quantity');
  const quantityValue = cart.querySelector('.product-purchase-cart-quantity-value');

  const productId = cart.dataset.productId;

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
    alert('Error adding product to cart. See JS console');
  });
};

const incrementProductQuantityInCart = (cart) => {
  const quantityValue = cart.querySelector('.product-purchase-cart-quantity-value');

  const productId = cart.dataset.productId;

  fetch(`/cart/add/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => quantityValue.textContent = (parseInt(quantityValue.textContent.trim()) + 1).toString())
  .catch(error => {
    console.error('Error:', error);
    alert('Error incrementing product quantity in cart. See JS console');
  });
};

const decrementProductQuantityInCart = (cart) => {
  const addBtn = cart.querySelector('.product-purchase-cart-add');
  const removeBtn = cart.querySelector('.product-purchase-cart-remove');
  const quantity = cart.querySelector('.product-purchase-cart-quantity');
  const quantityValue = cart.querySelector('.product-purchase-cart-quantity-value');

  const productId = cart.dataset.productId;

  fetch(`/cart/decrease/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
  })
  .then(() => {
    quantityValue.textContent = (parseInt(quantityValue.textContent.trim()) - 1).toString();
    if (quantityValue.textContent === '0') {
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

const createProduct = (event) => {
  const formData = new FormData(form);

  fetch(form.action, {
    method: 'POST',
    body: formData,
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }
    })
    .then(() => {
      closeModal();      
      window.location.href = window.location.href; // Reload current page
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error product creation. See JS console');
    });

  event.preventDefault();
};

const changePage = (pageButton) => {
  const page = pageButton.dataset.page;
  const url = new URL(window.location.href);
  url.searchParams.set('page', page);
  window.location.href = url.toString();
};

const changePageSize = () => {
  const newPageSizeValue = pageSize.value;
  const currentUrl = new URL(window.location.href);
  const params = currentUrl.searchParams;
  params.set('size', newPageSizeValue);
  params.set('page', 0); // Request first page if we changed page size

  window.location.href = currentUrl.toString(); // Reload current page
};

const setUpPageSizeValue = () => {
    const params = new URLSearchParams(window.location.search);
    const pageSizeValue = parseInt(params.get('size')) || defaultPageSizeValue;
    pageSize.value = pageSizeValue;
}

const setUpTextFilterInputValue = () => {
    const params = new URLSearchParams(window.location.search);
    const textFilterInputValue = params.get('search') || defaultTextFilterInputValue;
    textFilterInput.value = textFilterInputValue;
}

const setUpProductQuantities = (cart) => {
  const addBtn = cart.querySelector('.product-purchase-cart-add');
  const removeBtn = cart.querySelector('.product-purchase-cart-remove');
  const quantity = cart.querySelector('.product-purchase-cart-quantity');
  const quantityValue = cart.querySelector('.product-purchase-cart-quantity-value');

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

// SetUp
setUpPageSizeValue()
setUpTextFilterInputValue ()
carts.forEach(
  cart => setUpProductQuantities(cart)
);

// Event listeners

// Open Modal
productCreateBtn.addEventListener('click', () => openModal());

// Close Modal (when clicking outside the modal)
modalOverlay.addEventListener('click', (event) => {
  if (event.target === modalOverlay) {
    closeModal();
  }
});

// Get Products with text filter
textFilterInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    const search = textFilterInput.value.trim();
    getProductsWithTextFilter(search);
  }
});


// Increment product quantity in cart
increments.forEach(
  increment => increment.addEventListener('click', () => incrementProductQuantityInCart(increment.parentElement.parentElement))
);

// Decrement product quantity in cart
decrements.forEach(
  decrement => decrement.addEventListener('click', () => decrementProductQuantityInCart(decrement.parentElement.parentElement))
);

// Add product to cart
adds.forEach(
  add => add.addEventListener('click', () => addProductToCart(add.parentElement))
);

// Remove product from cart
removals.forEach(
  remove => remove.addEventListener('click', () => removeProductFromCart(remove.parentElement))
);

// Create new product
form.addEventListener('submit', (event) => createProduct(event));

// Change page
document.addEventListener('DOMContentLoaded', () => {
    const pageButtons = document.querySelectorAll('.page-button');
    pageButtons.forEach((pageButton) => {
        if (pageButton.classList.contains('active')) {
            return;
        }
        pageButton.addEventListener('click', () => changePage(pageButton));
    });
});

// Change page size
pageSize.addEventListener('change', () => changePageSize());
