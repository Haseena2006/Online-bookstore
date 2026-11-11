/* Shopping cart page. All numbers come from the server; the page re-renders from each response. */
(function () {
  'use strict';
  const B = window.Bookstore;

  B.pages.cart = async function () {
    const list = B.$('#cart-list'), summary = B.$('#cart-summary');

    if (!B.user) {
      list.innerHTML = `<div class="state"><h2>Log in to see your cart</h2><p>Your cart is saved to your account.</p>
        <a class="btn btn-primary" href="${B.CTX}/login.html?msg=cart&next=cart.html">Log in</a>
        <a class="btn btn-outline" href="${B.CTX}/register.html">Create an account</a></div>`;
      summary.hidden = true;
      return;
    }

    function render(cart) {
      B.setCartCount(cart.totalItems);
      if (!cart.items.length) {
        list.innerHTML = `<div class="state"><h2>Your cart is empty</h2><p>Find something good to read.</p><a class="btn btn-primary" href="${B.CTX}/books.html">Browse books</a></div>`;
        summary.hidden = true;
        return;
      }
      summary.hidden = false;
      list.innerHTML = cart.items.map((it) => `
        <article class="cart-item" data-id="${it.id}">
          <img src="${B.img(it.imageUrl)}" alt="" onerror="this.onerror=null;this.src='${B.CTX}/images/placeholder.svg'">
          <div>
            <h3><a href="${B.CTX}/book-details.html?id=${it.bookId}">${B.esc(it.title)}</a></h3>
            <p class="muted">${B.esc(it.author)} &middot; ${B.money(it.price)} each</p>
            <div class="qty" role="group" aria-label="Quantity for ${B.esc(it.title)}">
              <button type="button" data-act="dec" aria-label="Decrease quantity" ${it.quantity <= 1 ? 'disabled' : ''}>&minus;</button>
              <input type="number" value="${it.quantity}" min="1" max="${it.stock}" data-act="set" aria-label="Quantity">
              <button type="button" data-act="inc" aria-label="Increase quantity" ${it.quantity >= it.stock ? 'disabled' : ''}>+</button>
            </div>
            ${it.quantity >= it.stock ? `<p class="hint" style="margin:6px 0 0">Maximum available: ${it.stock}</p>` : ''}
          </div>
          <div class="cart-item-side">
            <span class="line-total">${B.money(it.lineTotal)}</span>
            <button class="btn btn-ghost" data-act="remove">Remove</button>
          </div>
        </article>`).join('');
      B.$('#sum-items').textContent = cart.totalItems;
      B.$('#sum-subtotal').textContent = B.money(cart.totalAmount);
      B.$('#sum-total').textContent = B.money(cart.totalAmount);
    }

    async function call(path, method, body) {
      try {
        const cart = await B.api(path, { method, body });
        render(cart);
      } catch (e) {
        if (e.status === 401) { location.href = B.CTX + '/login.html?msg=cart&next=cart.html'; return; }
        B.toast(e.message, 'error');
        load();   // resync with the server after a rejected change
      }
    }

    async function load() {
      try { render(await B.api('/cart')); }
      catch (e) { list.innerHTML = `<div class="state"><h2>We couldn't load your cart</h2><p>${B.esc(e.message)}</p><button class="btn btn-dark" onclick="location.reload()">Try again</button></div>`; summary.hidden = true; }
    }

    list.addEventListener('click', (e) => {
      const el = e.target.closest('[data-act]');
      if (!el || el.tagName === 'INPUT') return;
      const row = el.closest('.cart-item'), id = row.dataset.id;
      const qty = Number(B.$('input', row).value);
      if (el.dataset.act === 'inc') call('/cart/' + id, 'PUT', { quantity: qty + 1 });
      if (el.dataset.act === 'dec') call('/cart/' + id, 'PUT', { quantity: qty - 1 });
      if (el.dataset.act === 'remove') call('/cart/' + id, 'DELETE').then(() => B.toast('Removed from your cart.', 'success'));
    });
    list.addEventListener('change', (e) => {
      if (e.target.dataset.act !== 'set') return;
      const id = e.target.closest('.cart-item').dataset.id;
      const q = parseInt(e.target.value, 10);
      if (!Number.isFinite(q) || q < 1) { B.toast('Quantity must be at least 1.', 'error'); load(); return; }
      call('/cart/' + id, 'PUT', { quantity: q });
    });

    load();
  };
})();
