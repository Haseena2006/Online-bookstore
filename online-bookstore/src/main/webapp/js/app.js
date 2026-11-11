/* Shared code: API client, header/footer, toasts, book cards, auth pages, home page. */
(function () {
  'use strict';

  // All pages live in the web root, so the folder of the current page is the context path
  // (e.g. "/online-bookstore" on Tomcat, "" when deployed as ROOT).
  const CTX = location.pathname.replace(/\/[^\/]*$/, '');

  const B = (window.Bookstore = {
    CTX,
    user: null,
    pages: {},
    cartCount: 0,
  });

  /* ---------- helpers ---------- */
  B.esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  B.money = (n) => '₹' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
  B.img = (url) => B.esc(url ? CTX + '/' + url.replace(/^\//, '') : CTX + '/images/placeholder.svg');
  B.$ = (sel, root = document) => root.querySelector(sel);
  B.$$ = (sel, root = document) => [...root.querySelectorAll(sel)];

  /* ---------- API client ---------- */
  class ApiError extends Error {
    constructor(status, message) { super(message); this.status = status; }
  }
  B.ApiError = ApiError;

  B.api = async function (path, options = {}) {
    const init = { method: options.method || 'GET', credentials: 'same-origin', headers: { Accept: 'application/json' } };
    if (options.body !== undefined) {
      init.headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(options.body);
    }
    let res;
    try {
      res = await fetch(CTX + path, init);
    } catch (e) {
      throw new ApiError(0, 'Cannot reach the server. Check your connection and try again.');
    }
    let data = null;
    const text = await res.text();
    if (text) { try { data = JSON.parse(text); } catch (e) { data = null; } }
    if (!res.ok) throw new ApiError(res.status, (data && data.message) || 'Request failed (' + res.status + ').');
    return data;
  };

  /* ---------- toasts ---------- */
  B.toast = function (message, type = 'info') {
    let box = B.$('.toasts');
    if (!box) {
      box = document.createElement('div');
      box.className = 'toasts';
      box.setAttribute('role', 'status');
      box.setAttribute('aria-live', 'polite');
      document.body.appendChild(box);
    }
    const t = document.createElement('div');
    t.className = 'toast ' + type;
    t.textContent = message;
    box.appendChild(t);
    setTimeout(() => t.remove(), 4200);
  };

  /* ---------- header / footer ---------- */
  const ICON_SEARCH = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>';
  const ICON_CART = '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="20" r="1.5"/><circle cx="18" cy="20" r="1.5"/><path d="M2 3h3l2.6 12.2a2 2 0 0 0 2 1.6h7.7a2 2 0 0 0 2-1.5L21 8H6"/></svg>';

  function renderHeader() {
    const page = document.body.dataset.page;
    const cur = (p) => (page === p ? ' aria-current="page"' : '');
    const q = new URLSearchParams(location.search).get('keyword') || '';
    const host = B.$('#site-header');
    host.className = 'site-header';
    host.innerHTML = `
      <div class="container nav" id="nav">
        <a class="brand" href="${CTX}/index.html"><img src="${CTX}/images/logo.svg" alt="">Page &amp; Co.</a>
        <button class="nav-toggle" id="nav-toggle" aria-label="Menu" aria-expanded="false">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 6h16M4 12h16M4 18h16"/></svg>
        </button>
        <nav class="nav-links" aria-label="Main">
          <a href="${CTX}/index.html"${cur('home')}>Home</a>
          <a href="${CTX}/books.html"${cur('books')}>Books</a>
          <a href="${CTX}/index.html#categories">Categories</a>
          <a href="${CTX}/index.html#about">About us</a>
          <a href="${CTX}/index.html#contact">Contact us</a>
        </nav>
        <form class="search" id="search-form" role="search" action="${CTX}/books.html">
          <label class="visually-hidden" for="search-input">Search books by title or author</label>
          <input id="search-input" name="keyword" type="search" placeholder="Search title or author" value="${B.esc(q)}" autocomplete="off">
          <button type="submit" aria-label="Search">${ICON_SEARCH}</button>
        </form>
        <div class="nav-actions" id="nav-actions"></div>
      </div>`;
    B.$('#nav-toggle').addEventListener('click', (e) => {
      const open = B.$('#nav').classList.toggle('open');
      e.currentTarget.setAttribute('aria-expanded', open);
    });
    renderNavActions();
  }

  function renderNavActions() {
    const box = B.$('#nav-actions');
    if (!box) return;
    const cart = `<a class="icon-link" href="${CTX}/cart.html" aria-label="Shopping cart">${ICON_CART}<span class="badge" id="cart-badge" ${B.cartCount ? '' : 'hidden'}>${B.cartCount}</span></a>`;
    if (B.user) {
      box.innerHTML = `${cart}<a class="icon-link" href="${CTX}/orders.html">My orders</a>
        <span class="user-name" title="${B.esc(B.user.email)}">Hi, ${B.esc(B.user.fullName.split(' ')[0])}</span>
        <button class="btn btn-outline btn-sm" id="logout-btn" style="color:#fff;border-color:rgba(255,255,255,.5)">Log out</button>`;
      B.$('#logout-btn').addEventListener('click', async () => {
        try { await B.api('/users/logout', { method: 'POST' }); } catch (e) { /* session already gone */ }
        location.href = CTX + '/index.html';
      });
    } else {
      box.innerHTML = `${cart}<a class="icon-link" href="${CTX}/login.html">Log in</a><a class="btn btn-primary btn-sm" href="${CTX}/register.html">Sign up</a>`;
    }
  }

  B.setCartCount = function (n) {
    B.cartCount = n;
    const badge = B.$('#cart-badge');
    if (badge) { badge.textContent = n; badge.hidden = !n; }
  };

  function renderFooter() {
    const host = B.$('#site-footer');
    host.className = 'site-footer';
    host.innerHTML = `
      <div class="container">
        <div class="footer-grid">
          <div><h3>Page &amp; Co.</h3><p>An independent online bookstore. Browse, order and pay when your books arrive.</p></div>
          <div><h3>Shop</h3><ul><li><a href="${CTX}/books.html">All books</a></li><li><a href="${CTX}/index.html#categories">Categories</a></li><li><a href="${CTX}/cart.html">Your cart</a></li><li><a href="${CTX}/orders.html">Your orders</a></li></ul></div>
          <div><h3>Account</h3><ul><li><a href="${CTX}/login.html">Log in</a></li><li><a href="${CTX}/register.html">Create an account</a></li></ul></div>
          <div id="contact-footer"><h3>Contact</h3><ul><li>12 Library Lane, Chennai 600001</li><li><a href="mailto:hello@pageandco.example">hello@pageandco.example</a></li><li>+91 90000 00000</li></ul></div>
        </div>
        <div class="footer-bottom">&copy; ${new Date().getFullYear()} Page &amp; Co. Demo project: orders are saved to the database, but no payment is taken.</div>
      </div>`;
  }

  /* ---------- book card ---------- */
  B.stockInfo = function (b) {
    if (b.stock <= 0) return { cls: 'out', text: 'Out of stock' };
    if (b.stock <= 5) return { cls: 'low', text: 'Only ' + b.stock + ' left' };
    return { cls: 'in', text: 'In stock' };
  };

  B.bookCard = function (b) {
    const s = B.stockInfo(b);
    const href = CTX + '/book-details.html?id=' + b.id;
    return `
      <article class="card">
        <a class="card-cover" href="${href}" tabindex="-1" aria-hidden="true"><img src="${B.img(b.imageUrl)}" alt="" loading="lazy" onerror="this.onerror=null;this.src='${CTX}/images/placeholder.svg'"></a>
        <div class="card-body">
          <h3 class="card-title"><a href="${href}">${B.esc(b.title)}</a></h3>
          <p class="card-author">${B.esc(b.author)}</p>
          <span class="chip">${B.esc(b.category)}</span>
          <div class="card-meta"><span class="price">${B.money(b.price)}</span><span class="stock ${s.cls}">${s.text}</span></div>
          <div class="card-actions">
            <a class="btn btn-outline" href="${href}">View details</a>
            <button class="btn btn-primary" data-add="${b.id}" ${b.stock <= 0 ? 'disabled' : ''}>Add to cart</button>
          </div>
        </div>
      </article>`;
  };

  /** Click handler for every "Add to cart" button inside a container. */
  B.bindAddToCart = function (root, onDone) {
    root.addEventListener('click', async (e) => {
      const btn = e.target.closest('[data-add]');
      if (!btn) return;
      btn.disabled = true;
      try {
        await B.addToCart(Number(btn.dataset.add), 1);
        if (onDone) onDone();
      } finally { btn.disabled = false; }
    });
  };

  B.addToCart = async function (bookId, quantity) {
    if (!B.user) {
      location.href = CTX + '/login.html?msg=cart&next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search);
      return false;
    }
    try {
      const cart = await B.api('/cart', { method: 'POST', body: { bookId, quantity } });
      B.setCartCount(cart.totalItems);
      B.toast('Added to your cart.', 'success');
      return true;
    } catch (e) {
      if (e.status === 401) { B.user = null; location.href = CTX + '/login.html?msg=cart'; return false; }
      B.toast(e.message, 'error');
      return false;
    }
  };

  B.skeletons = (n) => Array.from({ length: n }, () => '<div class="skeleton"></div>').join('');

  /* ---------- session bootstrap ---------- */
  async function loadSession() {
    try {
      B.user = await B.api('/users/me');
      const cart = await B.api('/cart');
      B.cartCount = cart.totalItems;
    } catch (e) {
      B.user = null;
      B.cartCount = 0;
    }
  }

  /* ---------- home page ---------- */
  const SPINES = [['#7a2e3b', '#f6e7c1'], ['#1f4d3a', '#f3e9c6'], ['#e0a526', '#13213f'], ['#2d4a8a', '#f6e7c1'], ['#5c3a1e', '#f0d9a8'], ['#a8433a', '#fff2d6'], ['#37556b', '#f6e7c1']];

  B.pages.home = async function () {
    const shelf = B.$('#shelf');
    const sections = [['featured', '#featured-grid'], ['popular', '#popular-grid'], ['new', '#new-grid']];
    sections.forEach(([, sel]) => (B.$(sel).innerHTML = B.skeletons(4)));
    try {
      const [featured, popular, fresh, cats] = await Promise.all([
        B.api('/books/featured'), B.api('/books/popular'), B.api('/books/new'), B.api('/books/categories'),
      ]);
      // Hero shelf: featured books as spines
      shelf.innerHTML = featured.slice(0, 9).map((b, i) => {
        const [bg, fg] = SPINES[(b.id * 3 + i) % SPINES.length];
        const h = 210 + ((b.id * 37) % 6) * 24;
        return `<a class="spine${i === 4 ? ' lean' : ''}" style="--h:${h}px;--bg:${bg};--fg:${fg};--i:${i}" href="${CTX}/book-details.html?id=${b.id}" title="${B.esc(b.title)} by ${B.esc(b.author)}"><span>${B.esc(b.title)}</span></a>`;
      }).join('') || '<p class="shelf-empty">New books arrive soon.</p>';

      B.$('#featured-grid').innerHTML = featured.slice(0, 4).map(B.bookCard).join('');
      B.$('#popular-grid').innerHTML = popular.slice(0, 4).map(B.bookCard).join('');
      B.$('#new-grid').innerHTML = fresh.slice(0, 4).map(B.bookCard).join('');
      B.$('#cat-grid').innerHTML = cats.map((c) =>
        `<a class="cat" href="${CTX}/books.html?category=${encodeURIComponent(c.name)}"><strong>${B.esc(c.name)}</strong><small>${c.count} book${c.count == 1 ? '' : 's'}</small></a>`).join('');
    } catch (e) {
      sections.forEach(([, sel]) => (B.$(sel).innerHTML = ''));
      B.$('#home-error').innerHTML = `<div class="state"><h2>We couldn't load the books</h2><p>${B.esc(e.message)}</p><button class="btn btn-dark" onclick="location.reload()">Try again</button></div>`;
    }
    B.$$('main .grid').forEach((g) => B.bindAddToCart(g));
  };

  /* ---------- login / register pages ---------- */
  function setErr(form, name, msg) {
    const g = form.querySelector('[data-field="' + name + '"]');
    if (!g) return;
    g.classList.toggle('invalid', !!msg);
    g.querySelector('.err').textContent = msg || '';
  }
  const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  B.EMAIL_RE = EMAIL_RE;
  B.PHONE_RE = /^\+?[0-9]{10,13}$/;
  B.setErr = setErr;

  function safeNext() {
    const n = new URLSearchParams(location.search).get('next') || 'index.html';
    return /^[A-Za-z0-9_\-]+\.html(\?[A-Za-z0-9_=&%\-]*)?$/.test(n) ? n : 'index.html';
  }

  B.pages.login = function () {
    if (B.user) { location.replace(CTX + '/' + safeNext()); return; }
    const params = new URLSearchParams(location.search);
    if (params.get('msg') === 'cart') B.$('#login-notice').hidden = false;
    if (params.get('registered')) B.$('#registered-notice').hidden = false;
    const form = B.$('#login-form');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = form.email.value.trim(), password = form.password.value;
      B.$('#form-error').classList.remove('show');
      setErr(form, 'email', EMAIL_RE.test(email) ? '' : 'Enter a valid email address.');
      setErr(form, 'password', password ? '' : 'Enter your password.');
      if (!EMAIL_RE.test(email) || !password) return;
      const btn = form.querySelector('button[type=submit]');
      btn.disabled = true;
      try {
        await B.api('/users/login', { method: 'POST', body: { email, password } });
        location.href = CTX + '/' + safeNext();
      } catch (err) {
        const box = B.$('#form-error');
        box.textContent = err.message; box.classList.add('show');
        btn.disabled = false;
      }
    });
  };

  B.pages.register = function () {
    if (B.user) { location.replace(CTX + '/index.html'); return; }
    const form = B.$('#register-form');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const v = { fullName: form.fullName.value.trim(), email: form.email.value.trim(), phone: form.phone.value.trim(), password: form.password.value };
      const confirm = form.confirm.value;
      B.$('#form-error').classList.remove('show');
      const errs = {
        fullName: v.fullName.length >= 2 ? '' : 'Enter your full name.',
        email: EMAIL_RE.test(v.email) ? '' : 'Enter a valid email address.',
        phone: !v.phone || B.PHONE_RE.test(v.phone.replace(/[\s-]/g, '')) ? '' : 'Phone number must have 10 to 13 digits.',
        password: v.password.length >= 8 && /[A-Za-z]/.test(v.password) && /[0-9]/.test(v.password) ? '' : 'Use at least 8 characters with a letter and a number.',
        confirm: v.password === confirm ? '' : 'Passwords do not match.',
      };
      Object.entries(errs).forEach(([k, m]) => setErr(form, k, m));
      if (Object.values(errs).some(Boolean)) return;
      const btn = form.querySelector('button[type=submit]');
      btn.disabled = true;
      try {
        await B.api('/users/register', { method: 'POST', body: v });
        // Log in straight away so the person can start shopping.
        await B.api('/users/login', { method: 'POST', body: { email: v.email, password: v.password } });
        location.href = CTX + '/index.html';
      } catch (err) {
        const box = B.$('#form-error');
        box.textContent = err.message; box.classList.add('show');
        btn.disabled = false;
      }
    });
  };

  /* ---------- start ---------- */
  document.addEventListener('DOMContentLoaded', async () => {
    renderHeader();
    renderFooter();
    await loadSession();
    renderNavActions();
    const init = B.pages[document.body.dataset.page];
    if (init) init();
  });
})();
