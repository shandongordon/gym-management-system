'use strict';
// Attendance & Check-In screen: shows the denial form and checks it before
// submitting. The server validates the same rules again.
document.addEventListener('DOMContentLoaded', () => {
    const denyButton = document.querySelector('#deny-button');
    const denyForm = document.querySelector('#deny-form');
    if (!denyButton || !denyForm) {
        return; // A decision was just recorded, or the member was not found.
    }
    const reason = document.querySelector('#deny-reason');
    const notes = document.querySelector('#deny-notes');
    const error = document.querySelector('#deny-error');
    const cancel = document.querySelector('#deny-cancel');

    function showError(text) {
        error.textContent = text;
        error.hidden = !text;
    }

    denyButton.addEventListener('click', () => {
        denyForm.hidden = false;
        reason.focus();
    });

    cancel.addEventListener('click', () => {
        denyForm.hidden = true;
        showError('');
    });

    denyForm.addEventListener('submit', (event) => {
        if (!reason.value) {
            event.preventDefault();
            showError('Select a reason for the denial.');
            return;
        }
        if (reason.value === 'Other' && !notes.value.trim()) {
            event.preventDefault();
            showError('Enter notes when the reason is "Other".');
            return;
        }
        showError('');
    });
});
