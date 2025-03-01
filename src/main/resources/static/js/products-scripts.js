// Elements
const modalOverlay = document.getElementById('modalOverlay');
const productCreateBtn = document.getElementById('productCreateBtn');
const form = document.getElementById('form');
const increments = document.querySelectorAll('.product-purchase-cart-quantity-stepper.increment');
const decrements = document.querySelectorAll('.product-purchase-cart-quantity-stepper.increment');
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

const addProductToCart = (event) => {
  const addBtn = event.target.closest('.product-purchase-cart-add');
  const removeBtn = event.target.closest('.product-purchase-cart-remove');
  const quantity = event.target.closest('.product-purchase-cart-quantity');

  const product = event.target.closest('.product');
  const productId = product.dataset.productId;

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
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error adding product to cart. See JS console');
  });
};

const incrementProductQuantityInCart = (event) => {
  const quantityValue = event.target.closest('product-purchase-cart-quantity-value');
  const product = event.target.closest('.product');
  const productId = product.dataset.productId;

  fetch(`/cart/increment/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
    return response.json();
  })
  .then(data => {
    data = {'count': 13};
    quantityValue.value = data['count'];
    // quantityValue.value  Увеличить счетчик на полученное значение
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Error incrementing product quantity in cart. See JS console');
  });
};

const decrementProductQuantityInCart = (event) => {
  const addBtn = event.target.closest('.product-purchase-cart-add');
  const removeBtn = event.target.closest('.product-purchase-cart-remove');
  const quantity = event.target.closest('.product-purchase-cart-quantity');
  const quantityValue = event.target.closest('product-purchase-cart-quantity-value');

  const product = event.target.closest('.product');
  const productId = product.dataset.productId;

  fetch(`/cart/decrement/${productId}`, {method: 'POST'})
  .then(response => {
    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }
    return response.json();
  })
  .then(data => {
    data = {'count': 13};
    if (data['count'] > 0) {
      quantityValue.value = data['count'];
    } else {
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

// SetUp
setUpPageSizeValue()

// Event listeners

// Open Modal
productCreateBtn.addEventListener('click', () => openModal());

// Close Modal (when clicking outside the modal)
modalOverlay.addEventListener('click', (event) => {
  if (event.target === modalOverlay) {
    closeModal();
  }
});

// Increment product quantity in cart
increments.forEach(
  increment => increment.addEventListener('click', (event) => incrementProductQuantityInCart(event))
);

// Decrement product quantity in cart
decrements.forEach(
  decrement => decrement.addEventListener('click', (event) => decrementProductQuantityInCart(event))
);

// Create new product
form.addEventListener('submit', (event) => createProduct(event));

// Change page
document.addEventListener('DOMContentLoaded', function () {
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
