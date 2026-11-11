/* Checkout page and order history page. */
(function () {
  'use strict';
  const B = window.Bookstore;

  B.pages.checkout = async function () {
    const wrap = B.$('#checkout-wrap');
    if (!B.user) {
      wrap.innerHTML = `<div class="state"><h2>Log in to check out</h2><p>We save your order to your account.</p>
        <a class="btn btn-primary" href="${B.CTX}/login.html?msg=cart&next=checkout.html">Log in</a></div>`;
      return;
    }
    let cart;
    try { cart = await B.api('/cart'); }
    catch (e) { wrap.innerHTML = `<div class="state"><h2>We couldn't load your cart</h2><p>${B.esc(e.message)}</p></div>`; return; }
    if (!cart.items.length) {
      wrap.innerHTML = `<div class="state"><h2>Your cart is empty</h2><p>Add a book before checking out.</p><a class="btn btn-primary" href="${B.CTX}/books.html">Browse books</a></div>`;
      return;
    }

    B.$('#co-lines').innerHTML = cart.items.map((i) =>
      `<div class="summary-line"><span>${B.esc(i.title)} &times; ${i.quantity}</span><span>${B.money(i.lineTotal)}</span></div>`).join('');
    B.$('#co-total').textContent = B.money(cart.totalAmount);

    const form = B.$('#checkout-form');
    form.fullName.value = B.user.fullName || '';
    form.email.value = B.user.email || '';
    form.phone.value = B.user.phone || '';

    const PIN_RE = /^[0-9]{6}$/;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const v = Object.fromEntries(['fullName', 'email', 'phone', 'address', 'city', 'pincode'].map((k) => [k, form[k].value.trim()]));
      const errs = {
        fullName: v.fullName.length >= 2 ? '' : 'Enter your full name.',
        email: B.EMAIL_RE.test(v.email) ? '' : 'Enter a valid email address.',
        phone: B.PHONE_RE.test(v.phone.replace(/[\s-]/g, '')) ? '' : 'Phone number must have 10 to 13 digits.',
        address: v.address.length >= 5 ? '' : 'Enter your delivery address.',
        city: v.city ? '' : 'Enter your city.',
        pincode: PIN_RE.test(v.pincode) ? '' : 'Pincode must be 6 digits.',
      };
      Object.entries(errs).forEach(([k, m]) => B.setErr(form, k, m));
      const box = B.$('#form-error'); box.classList.remove('show');
      const firstBad = Object.keys(errs).find((k) => errs[k]);
      if (firstBad) { form[firstBad].focus(); return; }

      const btn = B.$('#confirm-btn');
      btn.disabled = true; btn.textContent = 'Placing order…';
      try {
        const order = await B.api('/checkout', { method: 'POST', body: v });
        B.setCartCount(0);
        wrap.innerHTML = `
          <div class="success">
            <div class="tick" aria-hidden="true">&#10003;</div>
            <h1>Order placed</h1>
            <p>Thank you, ${B.esc(v.fullName.split(' ')[0])}. Your order number is</p>
            <div class="order-ref">${B.esc(order.orderRef)}</div>
            <p>Total to pay on delivery: <strong>${B.money(order.totalAmount)}</strong>.<br>We'll deliver to ${B.esc(v.address)}, ${B.esc(v.city)} - ${B.esc(v.pincode)}.</p>
            <a class="btn btn-primary" href="${B.CTX}/books.html">Continue shopping</a>
            <a class="btn btn-outline" href="${B.CTX}/orders.html">View my orders</a>
          </div>`;
        window.scrollTo({ top: 0 });
      } catch (err) {
        if (err.status === 401) { location.href = B.CTX + '/login.html?msg=cart&next=checkout.html'; return; }
        box.textContent = err.message; box.classList.add('show');
        B.toast(err.message, 'error');
        btn.disabled = false; btn.textContent = 'Confirm order';
        if (err.status === 409) B.$('#fix-cart').hidden = false;
      }
    });
  };

  B.pages.orders = async function () {
    const list = B.$('#orders-list');
    if (!B.user) {
      list.innerHTML = `<div class="state"><h2>Log in to see your orders</h2><a class="btn btn-primary" href="${B.CTX}/login.html?next=orders.html">Log in</a></div>`;
      return;
    }
    try {
      const orders = await B.api('/users/' + B.user.id + '/orders');
      if (!orders.length) {
        list.innerHTML = `<div class="state"><h2>No orders yet</h2><p>Your orders will show up here.</p><a class="btn btn-primary" href="${B.CTX}/books.html">Browse books</a></div>`;
        return;
      }
      list.innerHTML = orders.map((o) => `
        <article class="order">
          <div class="order-head">
            <div><b>${B.esc(o.orderRef)}</b><br><small>${new Date(o.orderDate).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</small></div>
            <div><span class="status">${B.esc(o.orderStatus)}</span></div>
            <div><b>${B.money(o.totalAmount)}</b><br><small>Cash on delivery</small></div>
          </div>
          <div class="order-body">
            ${o.items.map((i) => `<div class="order-line"><img src="${B.img(i.imageUrl)}" alt=""><span>${B.esc(i.title)} &times; ${i.quantity}</span><span>${B.money(i.lineTotal)}</span></div>`).join('')}
            <div class="address">${B.esc(o.deliveryAddress)}</div>
          </div>
        </article>`).join('');
    } catch (e) {
      list.innerHTML = `<div class="state"><h2>We couldn't load your orders</h2><p>${B.esc(e.message)}</p></div>`;
    }
  };
})();
