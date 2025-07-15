// Global variables
let currentUser = null;
let authToken = null;
let userAccounts = [];

// API Base URL
const API_BASE_URL = '/api';

// Initialize application
$(document).ready(function() {
    // Check if user is already logged in
    const token = localStorage.getItem('authToken');
    if (token) {
        authToken = token;
        validateTokenAndLoadDashboard();
    }
    
    // Bind form events
    bindFormEvents();
});

// Bind form events
function bindFormEvents() {
    $('#login-form').on('submit', handleLogin);
    $('#register-form').on('submit', handleRegister);
    $('#transfer-form').on('submit', handleTransfer);
    $('#create-account-form').on('submit', handleCreateAccount);
}

// Authentication functions
function handleLogin(e) {
    e.preventDefault();
    
    const username = $('#login-username').val();
    const password = $('#login-password').val();
    
    const loginData = { username, password };
    
    $.ajax({
        url: `${API_BASE_URL}/auth/login`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(loginData),
        success: function(response) {
            authToken = response.accessToken;
            localStorage.setItem('authToken', authToken);
            showAlert('Login successful!', 'success');
            loadDashboard();
        },
        error: function(xhr) {
            const error = xhr.responseJSON ? xhr.responseJSON.message : 'Login failed';
            showAlert(error, 'danger');
        }
    });
}

function handleRegister(e) {
    e.preventDefault();
    
    const firstName = $('#register-firstname').val();
    const lastName = $('#register-lastname').val();
    const username = $('#register-username').val();
    const email = $('#register-email').val();
    const password = $('#register-password').val();
    
    const registerData = { firstName, lastName, username, email, password };
    
    $.ajax({
        url: `${API_BASE_URL}/auth/signup`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(registerData),
        success: function(response) {
            showAlert('Registration successful! Please login.', 'success');
            showLogin();
        },
        error: function(xhr) {
            const error = xhr.responseJSON ? xhr.responseJSON.message : 'Registration failed';
            showAlert(error, 'danger');
        }
    });
}

function validateTokenAndLoadDashboard() {
    $.ajax({
        url: `${API_BASE_URL}/users/me`,
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`
        },
        success: function(response) {
            currentUser = response;
            loadDashboard();
        },
        error: function() {
            logout();
        }
    });
}

function logout() {
    authToken = null;
    currentUser = null;
    userAccounts = [];
    localStorage.removeItem('authToken');
    
    $('#nav-user').addClass('d-none');
    $('#nav-guest').removeClass('d-none');
    $('#dashboard-section').addClass('d-none');
    $('#login-section').removeClass('d-none');
    $('#register-section').addClass('d-none');
    
    showAlert('Logged out successfully', 'info');
}

// Navigation functions
function showLogin() {
    $('#login-section').removeClass('d-none');
    $('#register-section').addClass('d-none');
    $('#dashboard-section').addClass('d-none');
}

function showRegister() {
    $('#register-section').removeClass('d-none');
    $('#login-section').addClass('d-none');
    $('#dashboard-section').addClass('d-none');
}

function loadDashboard() {
    $('#login-section').addClass('d-none');
    $('#register-section').addClass('d-none');
    $('#dashboard-section').removeClass('d-none');
    
    $('#nav-guest').addClass('d-none');
    $('#nav-user').removeClass('d-none');
    $('#user-name').text(currentUser ? currentUser.firstName : 'User');
    
    loadAccounts();
    showSection('accounts');
}

// Section navigation
function showSection(section) {
    $('.content-section').addClass('d-none');
    $(`#${section}-content`).removeClass('d-none');
    
    switch(section) {
        case 'accounts':
            loadAccounts();
            break;
        case 'transfer':
            loadAccountsForDropdown();
            break;
        case 'history':
            loadAccountsForHistory();
            break;
    }
}

// Account management
function loadAccounts() {
    $.ajax({
        url: `${API_BASE_URL}/accounts`,
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`
        },
        success: function(accounts) {
            userAccounts = accounts;
            displayAccounts(accounts);
            updateDashboardSummary(accounts);
        },
        error: function(xhr) {
            showAlert('Failed to load accounts', 'danger');
        }
    });
}

function displayAccounts(accounts) {
    const accountsList = $('#accounts-list');
    accountsList.empty();
    
    if (accounts.length === 0) {
        accountsList.html('<p class="text-muted">No accounts found. Create your first account!</p>');
        return;
    }
    
    accounts.forEach(account => {
        const accountCard = `
            <div class="account-card p-3 mb-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">${account.accountType} Account</h6>
                        <p class="mb-1">Account: ${account.accountNumber}</p>
                        <h5 class="mb-0">${parseFloat(account.balance).toFixed(2)}</h5>
                    </div>
                    <div>
                        <button class="btn btn-sm btn-light me-2" onclick="showTransactionModal('deposit', ${account.id})">
                            <i class="fas fa-plus"></i>
                        </button>
                        <button class="btn btn-sm btn-light" onclick="showTransactionModal('withdraw', ${account.id})">
                            <i class="fas fa-minus"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
        accountsList.append(accountCard);
    });
}

function updateDashboardSummary(accounts) {
    const totalBalance = accounts.reduce((sum, account) => sum + parseFloat(account.balance), 0);
    $('#total-balance').text(`${totalBalance.toFixed(2)}`);
    $('#active-accounts').text(accounts.length);
}

function createAccount() {
    $('#createAccountModal').modal('show');
}

function submitCreateAccount() {
    const accountType = $('#account-type').val();
    
    if (!accountType) {
        showAlert('Please select an account type', 'warning');
        return;
    }
    
    $.ajax({
        url: `${API_BASE_URL}/accounts`,
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({ accountType }),
        success: function(response) {
            $('#createAccountModal').modal('hide');
            showAlert('Account created successfully!', 'success');
            loadAccounts();
        },
        error: function(xhr) {
            const error = xhr.responseJSON ? xhr.responseJSON.message : 'Failed to create account';
            showAlert(error, 'danger');
        }
    });
}

// Transaction functions
function showTransactionModal(type, accountId = null) {
    $('#transaction-type').val(type);
    $('#transactionModalTitle').text(type.charAt(0).toUpperCase() + type.slice(1) + ' Money');
    
    loadAccountsForTransaction();
    
    if (accountId) {
        $('#transaction-account').val(accountId);
    }
    
    $('#transactionModal').modal('show');
}

function loadAccountsForTransaction() {
    const select = $('#transaction-account');
    select.empty().append('<option value="">Select Account</option>');
    
    userAccounts.forEach(account => {
        select.append(`<option value="${account.id}">${account.accountType} - ${account.accountNumber} (${parseFloat(account.balance).toFixed(2)})</option>`);
    });
}

function submitTransaction() {
    const type = $('#transaction-type').val();
    const accountId = $('#transaction-account').val();
    const amount = $('#transaction-amount-modal').val();
    const description = $('#transaction-description-modal').val();
    
    if (!accountId || !amount) {
        showAlert('Please fill in all required fields', 'warning');
        return;
    }
    
    const transactionData = {
        accountId: parseInt(accountId),
        amount: parseFloat(amount),
        description: description || ''
    };
    
    $.ajax({
        url: `${API_BASE_URL}/transactions/${type}`,
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(transactionData),
        success: function(response) {
            $('#transactionModal').modal('hide');
            showAlert(`${type.charAt(0).toUpperCase() + type.slice(1)} successful!`, 'success');
            loadAccounts();
            
            // Clear form
            $('#transaction-form')[0].reset();
        },
        error: function(xhr) {
            const error = xhr.responseJSON ? xhr.responseJSON.message : `${type} failed`;
            showAlert(error, 'danger');
        }
    });
}

// Transfer functions
function loadAccountsForDropdown() {
    const fromSelect = $('#from-account');
    const toSelect = $('#to-account');
    
    fromSelect.empty().append('<option value="">Select Account</option>');
    toSelect.empty().append('<option value="">Select Account</option>');
    
    userAccounts.forEach(account => {
        const option = `<option value="${account.id}">${account.accountType} - ${account.accountNumber} (${parseFloat(account.balance).toFixed(2)})</option>`;
        fromSelect.append(option);
        toSelect.append(option);
    });
}

function handleTransfer(e) {
    e.preventDefault();
    
    const fromAccountId = $('#from-account').val();
    const toAccountId = $('#to-account').val();
    const amount = $('#transfer-amount').val();
    const description = $('#transfer-description').val();
    
    if (fromAccountId === toAccountId) {
        showAlert('Source and destination accounts cannot be the same', 'warning');
        return;
    }
    
    const transferData = {
        fromAccountId: parseInt(fromAccountId),
        toAccountId: parseInt(toAccountId),
        amount: parseFloat(amount),
        description: description || ''
    };
    
    $.ajax({
        url: `${API_BASE_URL}/transactions/transfer`,
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(transferData),
        success: function(response) {
            showAlert('Transfer successful!', 'success');
            loadAccounts();
            $('#transfer-form')[0].reset();
        },
        error: function(xhr) {
            const error = xhr.responseJSON ? xhr.responseJSON.message : 'Transfer failed';
            showAlert(error, 'danger');
        }
    });
}

// Transaction history
function loadAccountsForHistory() {
    const select = $('#history-account');
    select.empty().append('<option value="">Select Account</option>');
    
    userAccounts.forEach(account => {
        select.append(`<option value="${account.id}">${account.accountType} - ${account.accountNumber}</option>`);
    });
}

function loadTransactionHistory() {
    const accountId = $('#history-account').val();
    
    if (!accountId) {
        $('#transaction-history').empty();
        return;
    }
    
    $.ajax({
        url: `${API_BASE_URL}/transactions/history/${accountId}`,
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`
        },
        success: function(transactions) {
            displayTransactionHistory(transactions);
        },
        error: function(xhr) {
            showAlert('Failed to load transaction history', 'danger');
        }
    });
}

function displayTransactionHistory(transactions) {
    const historyContainer = $('#transaction-history');
    historyContainer.empty();
    
    if (transactions.length === 0) {
        historyContainer.html('<p class="text-muted">No transactions found</p>');
        return;
    }
    
    transactions.forEach(transaction => {
        const date = new Date(transaction.createdAt).toLocaleDateString();
        const time = new Date(transaction.createdAt).toLocaleTimeString();
        
        let typeClass = '';
        let typeIcon = '';
        let amountPrefix = '';
        
        switch(transaction.transactionType) {
            case 'DEPOSIT':
                typeClass = 'transaction-deposit';
                typeIcon = 'fas fa-plus-circle text-success';
                amountPrefix = '+';
                break;
            case 'WITHDRAWAL':
                typeClass = 'transaction-withdrawal';
                typeIcon = 'fas fa-minus-circle text-warning';
                amountPrefix = '-';
                break;
            case 'TRANSFER':
                typeClass = 'transaction-transfer';
                typeIcon = 'fas fa-exchange-alt text-info';
                amountPrefix = transaction.fromAccount?.id == $('#history-account').val() ? '-' : '+';
                break;
        }
        
        const transactionItem = `
            <div class="transaction-item ${typeClass}">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <i class="${typeIcon} me-3"></i>
                        <div>
                            <h6 class="mb-1">${transaction.transactionType}</h6>
                            <small class="text-muted">${date} ${time}</small>
                            ${transaction.description ? `<p class="mb-0 text-muted">${transaction.description}</p>` : ''}
                        </div>
                    </div>
                    <div class="text-end">
                        <h6 class="mb-0">${amountPrefix}${parseFloat(transaction.amount).toFixed(2)}</h6>
                        <small class="badge bg-${transaction.status === 'COMPLETED' ? 'success' : transaction.status === 'PENDING' ? 'warning' : 'danger'}">
                            ${transaction.status}
                        </small>
                    </div>
                </div>
            </div>
        `;
        
        historyContainer.append(transactionItem);
    });
}

// Utility functions
function showAlert(message, type) {
    const alertId = 'alert-' + Date.now();
    const alert = `
        <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    $('#alert-container').append(alert);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        $(`#${alertId}`).alert('close');
    }, 5000);
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

// Validate form inputs
function validateAmount(amount) {
    return amount && amount > 0;
}

function validateAccountSelection(accountId) {
    return accountId && accountId !== '';
}

// Real-time updates (WebSocket simulation with polling)
function startRealTimeUpdates() {
    if (authToken) {
        setInterval(() => {
            loadAccounts();
        }, 30000); // Update every 30 seconds
    }
}

// Initialize real-time updates when dashboard loads
$(document).ready(() => {
    startRealTimeUpdates();
});