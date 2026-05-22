async function loadCart() {
    const user = api.auth.getCurrentUser();
    const cartContent = document.getElementById('cartContent');
    const checkoutPanel = document.getElementById('checkoutPanel');
    const cartCount = document.getElementById('cartCount');

    try {
        cartContent.innerHTML = '<p>Cargando carrito...</p>';
        const cart = await api.get(`/cart/${user.id}`);
        
        if (!cart || !cart.items || cart.items.length === 0) {
            cartContent.innerHTML = '<p>Tu carrito está vacío.</p>';
            checkoutPanel.style.display = 'none';
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
            <button class="btn btn-primary btn-large mt-1" style="width:100%" onclick="document.getElementById('checkoutPanel').style.display='block'">Proceder al Pago</button>
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
    try {
        // 1. Guardar dirección
        const calle = document.getElementById('chkCalle').value;
        const ciudad = document.getElementById('chkCiudad').value;
        const pais = document.getElementById('chkPais').value;
        
        await api.post(`/checkout/${user.id}/address`, { calle, ciudad, departamento: ciudad, pais });
        
        // 2. Guardar método pago (Simulado)
        await api.post(`/checkout/${user.id}/payment`, { metodo: 'TARJETA_CREDITO' });

        // 3. Confirmar Orden
        await api.post(`/checkout/${user.id}/confirm`);

        alert("¡Compra realizada con éxito! Revisa tu biblioteca.");
        document.getElementById('nav-library').click(); // Ir a biblioteca
        
    } catch (error) {
        alert("Error en el checkout: " + error.message);
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
                    <h3 class="book-title">${libro.title}</h3>
                    <p class="book-author">${libro.author}</p>
                    <button class="btn btn-primary mt-1" onclick="downloadBook('${libro.id}', '${libro.filePath || ''}')">Descargar Seguro</button>
                </div>
            `;
            libraryGrid.appendChild(card);
        });

    } catch (error) {
        libraryGrid.innerHTML = `<p class="error-message" style="display:block">Error: ${error.message}</p>`;
    }
}

async function downloadBook(libroId, filePath) {
    if (filePath) {
        alert("Iniciando descarga desde tu biblioteca...");
        window.open(filePath, '_blank');
        return;
    }
    
    // Fallback if no real file path
    const user = api.auth.getCurrentUser();
    try {
        // Generar enlace
        const response = await api.post(`/downloads/generate`, { buyerId: user.id, libroId: libroId });
        const token = response.token;
        
        // Ejecutar descarga
        const downloadResp = await api.get(`/downloads/${token}`);
        alert("¡Descarga iniciada de forma segura!\nEnlace temporal: " + downloadResp.url);
    } catch (error) {
        alert("Error al descargar: " + error.message);
    }
}
