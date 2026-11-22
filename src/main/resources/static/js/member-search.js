'use strict';
document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#member-search-form');
    const search = document.querySelector('#member-query');
    const message = document.querySelector('#search-message');
    const resultsSection = document.querySelector('#results-section');
    const resultsBody = document.querySelector('#results-body');

    function announce(text, kind) {
        message.textContent = text;
        message.className = 'search-message ' + (kind || '');
    }

    function displayMembers(members) {
        resultsBody.replaceChildren();
        for (const member of members) {
            const tr = document.createElement('tr');
            for (const value of [member.memberId, member.fullName, member.email]) {
                const td = document.createElement('td');
                td.textContent = value;
                tr.appendChild(td);
            }
            const actionCell = document.createElement('td');
            const link = document.createElement('a');
            link.href = '/members/' + encodeURIComponent(member.memberId) + '/selected';
            link.className = 'btn-primary-app btn-small';
            link.textContent = 'Select Member';
            actionCell.appendChild(link);
            tr.appendChild(actionCell);
            resultsBody.appendChild(tr);
        }
        resultsSection.hidden = members.length === 0;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const query = search.value.trim();
        resultsSection.hidden = true;
        resultsBody.replaceChildren();
        if (!query) {
            announce('Please enter an ID or name before searching.', 'error');
            return;
        }
        announce('Searching...', '');
        try {
            const response = await fetch('/api/members?query=' + encodeURIComponent(query), {
                headers: {'Accept': 'application/json'}, credentials: 'same-origin'
            });
            if (response.redirected && response.url.includes('/login')) {
                location.assign('/login');
                return;
            }
            if (!response.ok) {
                const payload = await response.json();
                throw new Error(payload.message || 'Unable to search members.');
            }
            const members = await response.json();
            if (members.length === 0) {
                announce('No member found.', 'error');
                return;
            }
            displayMembers(members);
            announce(members.length + (members.length === 1 ? ' member found.' : ' members found.'), 'success');
        } catch (error) {
            announce(error.message || 'Unable to search members.', 'error');
        }
    });
});
