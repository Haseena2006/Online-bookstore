/* Book listing page (search, category filter, sort, pagination) and book details page. */
(function () {
  'use strict';
  const B = window.Bookstore;
  const PAGE_SIZE = 12;

  /* ---------- listing ---------- */
  B.pages.books = async function () {
    const params = new URLSearchParams(location.search);
    const state = {
      keyword: params.get('keyword') || '',
      category: params.get('category') || '',
      sort: params.get('sort') || '',
      page: Math.max(1, parseInt(params.get('page') || '1', 10) || 1),
    };
    const grid = B.$('#book-grid'), count = B.$('#result-count'), pager = B.$('#pagination');
    const kw = B.$('#f-keyword'), cat = B.$('#f-category'), sort = B.$('#f-sort');
    kw.value = state.keyword; sort.value = state.sort;

    // Category options come from the database
    try {
      const cats = await B.api('/books/categories');
      cat.innerHTML = '<option value="">All categories</option>' +
        cats.map((c) => `<option value="${B.esc(c.name)}">${B.esc(c.name)} (${c.count})</option>`).join('');
      cat.value = state.category;
    } catch (e) { /* the list still works without the filter options */ }

    let books = [];

    function syncUrl() {
      const p = new URLSearchParams();
      if (state.keyword) p.set('keyword', state.keyword);
      if (state.category) p.set('category', state.category);
      if (state.sort) p.set('sort', state.sort);
      if (state.page > 1) p.set('page', state.page);
      history.replaceState(null, '', location.pathname + (p.toString() ? '?' + p : ''));
    }

    function render() {
      const pages = Math.max(1, Math.ceil(books.length / PAGE_SIZE));
      state.page = Math.min(state.page, pages);
      const slice = books.slice((state.page - 1) * PAGE_SIZE, state.page * PAGE_SIZE);
      count.textContent = books.length + ' book' + (books.length === 1 ? '' : 's');
      if (!books.length) {
        grid.innerHTML = `<div class="state" style="grid-column:1/-1"><h2>No books match your search</h2><p>Try a different title, author or category.</p><button class="btn btn-dark" id="clear-filters">Clear filters</button></div>`;
        B.$('#clear-filters').onclick = () => { kw.value = ''; cat.value = ''; sort.value = ''; Object.assign(state, { keyword: '', category: '', sort: '', page: 1 }); load(); };
        pager.innerHTML = '';
        return;
      }
      grid.innerHTML = slice.map(B.bookCard).join('');
      pager.innerHTML = pages < 2 ? '' :
        `<button data-p="${state.page - 1}" ${state.page === 1 ? 'disabled' : ''}>Previous</button>` +
        Array.from({ length: pages }, (_, i) => `<button data-p="${i + 1}" ${i + 1 === state.page ? 'aria-current="page"' : ''}>${i + 1}</button>`).join('') +
        `<button data-p="${state.page + 1}" ${state.page === pages ? 'disabled' : ''}>Next</button>`;
    }

    async function load() {
      grid.innerHTML = B.skeletons(8);
      const q = new URLSearchParams();
      if (state.keyword) q.set('keyword', state.keyword);
      if (state.category) q.set('category', state.category);
      if (state.sort) q.set('sort', state.sort);
      try {
        books = await B.api('/books' + (q.toString() ? '?' + q : ''));
        render();
      } catch (e) {
        grid.innerHTML = `<div class="state" style="grid-column:1/-1"><h2>We couldn't load the books</h2><p>${B.esc(e.message)}</p><button class="btn btn-dark" onclick="location.reload()">Try again</button></div>`;
        pager.innerHTML = '';
      }
      syncUrl();
    }

    let timer;
    kw.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(() => { state.keyword = kw.value.trim(); state.page = 1; load(); }, 300); });
    B.$('#filters').addEventListener('submit', (e) => { e.preventDefault(); clearTimeout(timer); state.keyword = kw.value.trim(); state.page = 1; load(); });
    cat.addEventListener('change', () => { state.category = cat.value; state.page = 1; load(); });
    sort.addEventListener('change', () => { state.sort = sort.value; state.page = 1; load(); });
    pager.addEventListener('click', (e) => {
      const b = e.target.closest('button[data-p]');
      if (!b || b.disabled) return;
      state.page = Number(b.dataset.p); render(); syncUrl();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
    B.bindAddToCart(grid);
    load();
  };

  /* ---------- details ---------- */
  B.pages.details = async function () {
    const root = B.$('#details');
    const id = Number(new URLSearchParams(location.search).get('id'));
    if (!Number.isInteger(id) || id <= 0) { notFound('That book link is not valid.'); return; }
    let b;
    try { b = await B.api('/books/' + id); }
    catch (e) { notFound(e.status === 404 ? 'We could not find that book.' : e.message); return; }

    document.title = b.title + ' | Page & Co.';
    const s = B.stockInfo(b);
    root.innerHTML = `
      <div class="details-cover"><img src="${B.img(b.imageUrl)}" alt="Cover of ${B.esc(b.title)}" onerror="this.onerror=null;this.src='${B.CTX}/images/placeholder.svg'"></div>
      <div>
        <span class="chip">${B.esc(b.category)}</span>
        <h1>${B.esc(b.title)}</h1>
        <p class="by">by ${B.esc(b.author)}</p>
        <span class="price">${B.money(b.price)}</span>
        <span class="stock ${s.cls}">${s.text}</span>
        <div class="buy-row">
          <div class="qty" role="group" aria-label="Quantity">
            <button type="button" id="q-minus" aria-label="Decrease quantity">&minus;</button>
            <input id="q-input" type="number" min="1" max="${b.stock}" value="1" aria-label="Quantity" ${b.stock <= 0 ? 'disabled' : ''}>
            <button type="button" id="q-plus" aria-label="Increase quantity">+</button>
          </div>
          <button class="btn btn-primary btn-lg" id="add-btn" ${b.stock <= 0 ? 'disabled' : ''}>Add to cart</button>
          <a class="btn btn-outline btn-lg" href="${B.CTX}/books.html">Back to books</a>
        </div>
        <p class="desc">${B.esc(b.description)}</p>
      </div>`;

    const input = B.$('#q-input'), minus = B.$('#q-minus'), plus = B.$('#q-plus');
    const clamp = () => {
      let v = parseInt(input.value, 10);
      if (!Number.isFinite(v) || v < 1) v = 1;
      if (v > b.stock) { v = Math.max(b.stock, 1); if (b.stock > 0) B.toast('Only ' + b.stock + ' in stock.', 'error'); }
      input.value = v;
      minus.disabled = v <= 1 || b.stock <= 0; plus.disabled = v >= b.stock;
    };
    minus.onclick = () => { input.value = Number(input.value) - 1; clamp(); };
    plus.onclick = () => { input.value = Number(input.value) + 1; clamp(); };
    input.onchange = clamp;
    clamp();

    const addBtn = B.$('#add-btn');
    addBtn.onclick = async () => {
      clamp();
      addBtn.disabled = true;
      await B.addToCart(b.id, Number(input.value));
      addBtn.disabled = b.stock <= 0;
    };

    function notFound(msg) {
      root.innerHTML = `<div class="state" style="grid-column:1/-1"><h2>Book not found</h2><p>${B.esc(msg)}</p><a class="btn btn-dark" href="${B.CTX}/books.html">Browse all books</a></div>`;
    }
  };
})();
