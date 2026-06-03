async function loadCart() {
    const user = api.auth.getCurrentUser();
    const cartContent = document.getElementById('cartContent');
    const cartCount = document.getElementById('cartCount');

    try {
        cartContent.innerHTML = '<p>Cargando carrito...</p>';
        const cart = await api.get(`/cart/${user.id}`);
        
        if (!cart || !cart.items || cart.items.length === 0) {
            cartContent.innerHTML = '<p>Tu carrito está vacío.</p>';
            if (cartCount) cartCount.textContent = '(0)';
            return;
        }

        let html = '<div class="cart-items" style="display:flex; flex-direction:column; gap:1rem;">';
        let total = 0;

        cart.items.forEach(item => {
            total += item.price;
            html += `
                <div style="display:flex; justify-content:space-between; align-items:center; background:var(--surface-light); padding:1rem; border-radius:8px;">
                    <div>
                        <h4 style="margin:0">${item.title}</h4>
                        <span style="color:var(--primary); font-weight:bold;">$${item.price.toFixed(2)}</span>
                    </div>
                    <button class="btn btn-danger" onclick="removeFromCart('${item.libroId}')">Eliminar</button>
                </div>
            `;
        });

        html += `
            <div style="text-align:right; margin-top:1.5rem; font-size:1.2rem;">
                Total a Pagar: <span style="color:var(--primary); font-weight:bold; font-size:1.5rem;">$${total.toFixed(2)}</span>
            </div>
            <button class="btn btn-primary btn-large mt-1" style="width:100%" onclick="processCheckout()">Confirmar Compra</button>
        </div>`;

        cartContent.innerHTML = html;
        if (cartCount) cartCount.textContent = `(${cart.items.length})`;
    } catch (error) {
        cartContent.innerHTML = `<p class="error-message" style="display:block">Error: ${error.message}</p>`;
    }
}

async function removeFromCart(libroId) {
    const user = api.auth.getCurrentUser();
    try {
        await api.delete(`/cart/${user.id}/items/${libroId}`);
        loadCart();
    } catch (error) {
        alert("Error al eliminar el item: " + error.message);
    }
}

async function processCheckout() {
    const user = api.auth.getCurrentUser();
    if (!confirm('¿Confirmar la compra de todos los libros en tu carrito?')) return;
    try {
        // Enviar datos dummy para cumplir con el proceso de checkout en el backend
        await api.post(`/checkout/${user.id}/address`, { calle: 'N/A', ciudad: 'N/A', departamento: 'N/A', pais: 'N/A' });
        await api.post(`/checkout/${user.id}/payment`, { metodo: 'TARJETA_CREDITO' });
        
        // Confirmar la orden
        await api.post(`/checkout/${user.id}/confirm`);
        alert('¡Compra realizada con éxito! Los libros ya están en tu biblioteca.');
        loadCart();
        document.getElementById('nav-library').click();
    } catch (error) {
        alert('Error en el pago: ' + error.message);
    }
}

async function loadLibrary() {
    const user = api.auth.getCurrentUser();
    const libraryGrid = document.getElementById('libraryGrid');

    try {
        libraryGrid.innerHTML = '<p>Cargando biblioteca...</p>';
        const libraries = await api.get(`/library/${user.id}`); // Esto era VerBibliotecaUseCase
        
        if (!libraries || libraries.length === 0) {
            libraryGrid.innerHTML = '<p>No tienes libros en tu biblioteca.</p>';
            return;
        }

        libraryGrid.innerHTML = '';
        libraries.forEach(libro => {
            const card = document.createElement('div');
            card.className = 'book-card';
            
            // Reutilizando estilos de catalog.js
            card.innerHTML = `
                <div style="background:var(--surface-light); height:200px; display:flex; align-items:center; justify-content:center;">
                    <span style="font-size:3rem; color:var(--primary)">📖</span>
                </div>
                <div class="book-info">
                    <h3 class="book-title">${libro.titulo || libro.title}</h3>
                    <p class="book-author">${libro.autor || libro.author}</p>
                    <div class="flex gap-1 mt-1">
                        <button class="btn btn-primary" style="flex:1" onclick="downloadBook('${libro.libroId || libro.id}', '')">⬇ Descargar</button>
                        <button class="btn btn-outline" style="flex:1" onclick="openLibraryReviewModal('${libro.libroId || libro.id}', '${(libro.titulo || libro.title || '').replace(/'/g, "\\'")}')">✍ Reseñar</button>
                    </div>
                </div>
            `;
            libraryGrid.appendChild(card);
        });

    } catch (error) {
        libraryGrid.innerHTML = `<p class="error-message" style="display:block">Error: ${error.message}</p>`;
    }
}

async function downloadBook(libroId, filePath) {
    // Si ya tenemos el filePath, abrirlo directamente
    if (filePath && filePath.trim() !== '') {
        window.open(filePath, '_blank');
        return;
    }
    // Si no, obtener el libro del backend y abrir su filePath
    try {
        const book = await api.get(`/books/${libroId}`);
        if (book && book.filePath && book.filePath.trim() !== '') {
            window.open(book.filePath, '_blank');
        } else {
            alert('Este libro no tiene un archivo de descarga disponible.');
        }
    } catch (error) {
        alert('Error al obtener el enlace de descarga: ' + error.message);
    }
}
