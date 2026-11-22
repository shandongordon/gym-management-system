'use strict';
document.addEventListener('DOMContentLoaded', () => {
    const parameters = new URLSearchParams(location.search);
    document.querySelector('#server-error').hidden = !parameters.has('error');
    document.querySelector('#logged-out').hidden = !parameters.has('logout');
    const form = document.querySelector('#loginForm');
    const warning = document.querySelector('#form-error');
    form.addEventListener('submit', (event) => {
        const username = document.querySelector('#username').value.trim();
        const password = document.querySelector('#password').value;
        if (!username || !password) {
            event.preventDefault();
            warning.textContent = 'Please enter both a username and password.';
            warning.classList.add('visible');
        } else {
            warning.classList.remove('visible');
            warning.textContent = '';
        }
    });
});
