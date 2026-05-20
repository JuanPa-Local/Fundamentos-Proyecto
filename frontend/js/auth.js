document.addEventListener('DOMContentLoaded', () => {
    
    // Si ya está logueado, redirigir
    const user = api.auth.getCurrentUser();
    if (user && window.location.pathname.includes('login.html')) {
        redirectByRole(user.role);
    }

    // Login logic
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('loginError');
            
            try {
                errorDiv.style.display = 'none';
                const user = await api.auth.login(email, password);
                redirectByRole(user.role);
            } catch (error) {
                errorDiv.textContent = error.message;
                errorDiv.style.display = 'block';
            }
        });
    }

    // Register logic
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const role = document.getElementById('role').value;
            const errorDiv = document.getElementById('registerError');

            try {
                errorDiv.style.display = 'none';
                if (role === 'BUYER') {
                    await api.auth.registerBuyer(fullName, email, password);
                } else if (role === 'SELLER') {
                    await api.auth.registerSeller(fullName, email, password);
                }
                alert("Registro exitoso. Ahora puedes iniciar sesión.");
                window.location.href = 'login.html';
            } catch (error) {
                errorDiv.textContent = error.message;
                errorDiv.style.display = 'block';
            }
        });
    }
});

function redirectByRole(role) {
    if (role === 'BUYER') window.location.href = 'dashboard-buyer.html';
    else if (role === 'SELLER') window.location.href = 'dashboard-seller.html';
    else if (role === 'ADMIN') window.location.href = 'dashboard-admin.html';
    else window.location.href = 'index.html';
}
