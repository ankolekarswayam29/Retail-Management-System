/**
 * RetailPro Enterprise Dashboard & Application JavaScript
 * High-End Dark Premium SaaS Analytics Engine
 */

// Global Chart References
window.retailProCharts = {
    monthlyRevenueChart: null,
    dailySalesChart: null,
    categoryChart: null,
    topProductsChart: null,
    inventoryChart: null,
    profitExpenseChart: null
};

document.addEventListener('DOMContentLoaded', function () {
    initThemeManager();
    initSidebar();
    initGlobalSearch();
    initAutoDismissAlerts();

    // Check if on Dashboard page and initialize analytics
    if (document.getElementById('monthlyRevenueChart')) {
        initDashboardAnalytics();
    }
});

/* ==========================================================================
   1. Theme Management (Dark / Light Toggle)
   ========================================================================== */
function initThemeManager() {
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    const moonIcon = document.querySelector('.theme-icon-dark');
    const sunIcon = document.querySelector('.theme-icon-light');

    // Retrieve saved preference or default to Dark
    const savedTheme = localStorage.getItem('retailpro-theme') || 'dark';
    applyTheme(savedTheme);

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', function () {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            applyTheme(newTheme);
            localStorage.setItem('retailpro-theme', newTheme);
            updateChartsTheme(newTheme);
        });
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        if (moonIcon && sunIcon) {
            if (theme === 'light') {
                moonIcon.classList.add('d-none');
                sunIcon.classList.remove('d-none');
            } else {
                moonIcon.classList.remove('d-none');
                sunIcon.classList.add('d-none');
            }
        }
    }
}

/* ==========================================================================
   2. Sidebar Navigation & Responsiveness
   ========================================================================== */
function initSidebar() {
    const collapseBtn = document.getElementById('sidebarCollapseBtn');
    const mobileToggleBtn = document.getElementById('mobileSidebarToggle');
    const sidebar = document.getElementById('appSidebar');
    const overlay = document.getElementById('sidebarOverlay');

    // Restore desktop collapsed state
    const isCollapsed = localStorage.getItem('retailpro-sidebar-collapsed') === 'true';
    if (isCollapsed && window.innerWidth >= 992) {
        document.body.classList.add('sidebar-collapsed');
    }

    if (collapseBtn) {
        collapseBtn.addEventListener('click', function () {
            document.body.classList.toggle('sidebar-collapsed');
            localStorage.setItem('retailpro-sidebar-collapsed', document.body.classList.contains('sidebar-collapsed'));
            // Trigger window resize to notify Chart.js to recalculate dimensions
            setTimeout(() => window.dispatchEvent(new Event('resize')), 300);
        });
    }

    if (mobileToggleBtn && sidebar && overlay) {
        mobileToggleBtn.addEventListener('click', function () {
            sidebar.classList.add('show-mobile');
            overlay.classList.add('show');
        });

        overlay.addEventListener('click', function () {
            sidebar.classList.remove('show-mobile');
            overlay.classList.remove('show');
        });
    }
}

/* ==========================================================================
   3. Global Search & Keyboard Shortcuts
   ========================================================================== */
function initGlobalSearch() {
    const searchInput = document.getElementById('globalSearchInput');

    window.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
    });

    if (searchInput) {
        searchInput.addEventListener('input', function (e) {
            const query = e.target.value.toLowerCase().trim();
            const tableRows = document.querySelectorAll('.table-dark-premium tbody tr');
            tableRows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        });
    }
}

/* ==========================================================================
   4. Auto-Dismiss Alerts
   ========================================================================== */
function initAutoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.classList.remove('show');
            setTimeout(() => alert.remove(), 150);
        }, 5000);
    });
}

/* ==========================================================================
   5. Interactive Dashboard Analytics (Chart.js Engine)
   ========================================================================== */
function getChartColors() {
    const isDark = (document.documentElement.getAttribute('data-theme') || 'dark') === 'dark';
    return {
        textColor: isDark ? '#9CA3AF' : '#4B5563',
        gridColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)',
        tooltipBg: isDark ? '#171F2B' : '#FFFFFF',
        tooltipText: isDark ? '#F9FAFB' : '#111827',
        tooltipBorder: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
        emerald: '#10B981',
        purple: '#8B5CF6',
        blue: '#3B82F6',
        amber: '#F59E0B',
        rose: '#EF4444',
        cyan: '#06B6D4'
    };
}

function initDashboardAnalytics() {
    // Read embedded data or fetch directly
    const rawDataScript = document.getElementById('initialAnalyticsData');
    let initialData = null;
    if (rawDataScript) {
        try {
            initialData = JSON.parse(rawDataScript.textContent);
        } catch (err) {
            console.error('Failed to parse initial analytics data', err);
        }
    }

    if (initialData) {
        renderAllCharts(initialData);
    } else {
        fetchAnalytics('month');
    }

    // Date Filter Segmented Buttons Listener
    const filterButtons = document.querySelectorAll('.date-filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            filterButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            const period = this.getAttribute('data-period');
            fetchAnalytics(period);
        });
    });
}

function fetchAnalytics(period) {
    showChartLoading(true);

    fetch(`/api/dashboard/analytics?period=${encodeURIComponent(period)}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            renderAllCharts(data);
            updatePeriodKpiSummary(data);
            showChartLoading(false);
        })
        .catch(error => {
            console.error('Error fetching analytics:', error);
            showChartLoading(false);
        });
}

function showChartLoading(show) {
    const overlays = document.querySelectorAll('.chart-loading-overlay');
    overlays.forEach(overlay => {
        if (show) {
            overlay.classList.add('active');
        } else {
            overlay.classList.remove('active');
        }
    });
}

function updatePeriodKpiSummary(data) {
    const periodRevElem = document.getElementById('periodRevenueDisplay');
    const periodCountElem = document.getElementById('periodSalesCountDisplay');
    if (periodRevElem && data.periodRevenue !== undefined) {
        periodRevElem.textContent = '$' + parseFloat(data.periodRevenue).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    if (periodCountElem && data.periodSalesCount !== undefined) {
        periodCountElem.textContent = data.periodSalesCount + ' orders';
    }
}

function renderAllCharts(data) {
    const colors = getChartColors();

    // 1. Monthly Revenue Line Chart
    renderMonthlyRevenueChart(data, colors);

    // 2. Daily Sales Bar Chart
    renderDailySalesChart(data, colors);

    // 3. Category-Wise Sales Doughnut Chart
    renderCategoryChart(data, colors);

    // 4. Best-Selling Products Chart
    renderTopProductsChart(data, colors);

    // 5. Inventory Stock Analysis Chart
    renderInventoryChart(data, colors);

    // 6. Profit and Expense Comparison Chart
    renderProfitExpenseChart(data, colors);
}

/* --- 1. Monthly Revenue Line Chart --- */
function renderMonthlyRevenueChart(data, colors) {
    const ctx = document.getElementById('monthlyRevenueChart');
    if (!ctx) return;

    if (window.retailProCharts.monthlyRevenueChart) {
        window.retailProCharts.monthlyRevenueChart.destroy();
    }

    const labels = data.monthlyLabels || ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const values = data.monthlyRevenue || [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

    const chartContext = ctx.getContext('2d');
    const gradient = chartContext.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(16, 185, 129, 0.35)');
    gradient.addColorStop(1, 'rgba(16, 185, 129, 0.0)');

    window.retailProCharts.monthlyRevenueChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Monthly Revenue ($)',
                data: values,
                borderColor: colors.emerald,
                borderWidth: 3,
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: colors.emerald,
                pointBorderColor: '#FFFFFF',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: function (ctx) {
                            return ' Revenue: $' + parseFloat(ctx.raw || 0).toLocaleString();
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: colors.gridColor, drawBorder: false },
                    ticks: { color: colors.textColor, font: { family: 'Plus Jakarta Sans', size: 11 } }
                },
                y: {
                    grid: { color: colors.gridColor, drawBorder: false },
                    ticks: {
                        color: colors.textColor,
                        font: { family: 'Plus Jakarta Sans', size: 11 },
                        callback: val => '$' + val.toLocaleString()
                    },
                    beginAtZero: true
                }
            }
        }
    });
}

/* --- 2. Daily Sales Bar Chart --- */
function renderDailySalesChart(data, colors) {
    const ctx = document.getElementById('dailySalesChart');
    if (!ctx) return;

    if (window.retailProCharts.dailySalesChart) {
        window.retailProCharts.dailySalesChart.destroy();
    }

    const labels = data.dailyLabels && data.dailyLabels.length > 0 ? data.dailyLabels : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const values = data.dailyRevenue && data.dailyRevenue.length > 0 ? data.dailyRevenue : [0, 0, 0, 0, 0, 0, 0];

    window.retailProCharts.dailySalesChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Daily Sales ($)',
                data: values,
                backgroundColor: colors.purple,
                borderRadius: 6,
                hoverBackgroundColor: '#A78BFA'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: ctx => ' Sales: $' + parseFloat(ctx.raw || 0).toLocaleString()
                    }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: colors.textColor, font: { family: 'Plus Jakarta Sans', size: 11 } }
                },
                y: {
                    grid: { color: colors.gridColor, drawBorder: false },
                    ticks: {
                        color: colors.textColor,
                        font: { family: 'Plus Jakarta Sans', size: 11 },
                        callback: val => '$' + val.toLocaleString()
                    },
                    beginAtZero: true
                }
            }
        }
    });
}

/* --- 3. Category-Wise Sales Doughnut Chart --- */
function renderCategoryChart(data, colors) {
    const ctx = document.getElementById('categorySalesChart');
    if (!ctx) return;

    if (window.retailProCharts.categoryChart) {
        window.retailProCharts.categoryChart.destroy();
    }

    const hasData = data.categoryLabels && data.categoryLabels.length > 0;
    const labels = hasData ? data.categoryLabels : ['No Category Data'];
    const values = hasData ? data.categoryValues : [1];
    const bgColors = hasData ?
        [colors.emerald, colors.purple, colors.blue, colors.amber, colors.cyan, colors.rose] :
        ['rgba(255, 255, 255, 0.1)'];

    window.retailProCharts.categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: bgColors,
                borderWidth: 0,
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '72%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: colors.textColor,
                        font: { family: 'Plus Jakarta Sans', size: 11, weight: '500' },
                        padding: 14,
                        boxWidth: 12,
                        boxHeight: 12,
                        borderRadius: 3
                    }
                },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: ctx => hasData ? ' ' + ctx.label + ': $' + parseFloat(ctx.raw || 0).toLocaleString() : ' No sales yet'
                    }
                }
            }
        }
    });
}

/* --- 4. Best-Selling Products Horizontal Bar Chart --- */
function renderTopProductsChart(data, colors) {
    const ctx = document.getElementById('topProductsChart');
    if (!ctx) return;

    if (window.retailProCharts.topProductsChart) {
        window.retailProCharts.topProductsChart.destroy();
    }

    const hasData = data.topProductLabels && data.topProductLabels.length > 0;
    const labels = hasData ? data.topProductLabels : ['No Sales Recorded'];
    const values = hasData ? data.topProductQuantities : [0];

    window.retailProCharts.topProductsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Units Sold',
                data: values,
                backgroundColor: colors.blue,
                borderRadius: 6,
                hoverBackgroundColor: '#60A5FA'
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: ctx => ' Units Sold: ' + ctx.raw
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: colors.gridColor, drawBorder: false },
                    ticks: { color: colors.textColor, font: { family: 'Plus Jakarta Sans', size: 11 } },
                    beginAtZero: true
                },
                y: {
                    grid: { display: false },
                    ticks: { color: colors.textColor, font: { family: 'Plus Jakarta Sans', size: 11 } }
                }
            }
        }
    });
}

/* --- 5. Inventory Stock Status Analysis --- */
function renderInventoryChart(data, colors) {
    const ctx = document.getElementById('inventoryStatusChart');
    if (!ctx) return;

    if (window.retailProCharts.inventoryChart) {
        window.retailProCharts.inventoryChart.destroy();
    }

    const labels = data.inventoryLabels || ['Healthy Stock', 'Low Stock Alert', 'Out of Stock'];
    const values = data.inventoryValues || [0, 0, 0];

    window.retailProCharts.inventoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [colors.emerald, colors.amber, colors.rose],
                borderWidth: 0,
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '72%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: colors.textColor,
                        font: { family: 'Plus Jakarta Sans', size: 11, weight: '500' },
                        padding: 14,
                        boxWidth: 12,
                        boxHeight: 12,
                        borderRadius: 3
                    }
                },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: ctx => ' ' + ctx.label + ': ' + ctx.raw + ' SKUs'
                    }
                }
            }
        }
    });
}

/* --- 6. Profit and Expense Comparison --- */
function renderProfitExpenseChart(data, colors) {
    const ctx = document.getElementById('profitExpenseChart');
    if (!ctx) return;

    if (window.retailProCharts.profitExpenseChart) {
        window.retailProCharts.profitExpenseChart.destroy();
    }

    const labels = data.profitExpenseLabels || ['Total Revenue', 'Cost of Goods', 'Gross Profit'];
    const values = data.profitExpenseValues || [0, 0, 0];

    window.retailProCharts.profitExpenseChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [colors.emerald, colors.rose, colors.purple],
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: colors.tooltipBg,
                    titleColor: colors.tooltipText,
                    bodyColor: colors.tooltipText,
                    borderColor: colors.tooltipBorder,
                    borderWidth: 1,
                    padding: 12,
                    callbacks: {
                        label: ctx => ' Amount: $' + parseFloat(ctx.raw || 0).toLocaleString()
                    }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: colors.textColor, font: { family: 'Plus Jakarta Sans', size: 11 } }
                },
                y: {
                    grid: { color: colors.gridColor, drawBorder: false },
                    ticks: {
                        color: colors.textColor,
                        font: { family: 'Plus Jakarta Sans', size: 11 },
                        callback: val => '$' + val.toLocaleString()
                    },
                    beginAtZero: true
                }
            }
        }
    });
}

function updateChartsTheme(theme) {
    const colors = getChartColors();
    Object.values(window.retailProCharts).forEach(chart => {
        if (!chart) return;
        if (chart.options.scales) {
            if (chart.options.scales.x && chart.options.scales.x.ticks) chart.options.scales.x.ticks.color = colors.textColor;
            if (chart.options.scales.x && chart.options.scales.x.grid) chart.options.scales.x.grid.color = colors.gridColor;
            if (chart.options.scales.y && chart.options.scales.y.ticks) chart.options.scales.y.ticks.color = colors.textColor;
            if (chart.options.scales.y && chart.options.scales.y.grid) chart.options.scales.y.grid.color = colors.gridColor;
        }
        if (chart.options.plugins && chart.options.plugins.legend && chart.options.plugins.legend.labels) {
            chart.options.plugins.legend.labels.color = colors.textColor;
        }
        chart.update();
    });
}
