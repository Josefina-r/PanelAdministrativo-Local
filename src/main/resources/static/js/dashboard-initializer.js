// Stub global para evitar "settingsController is not defined"
if (!window.settingsController) {
    window.settingsController = (function () {
        let _ready = false;
        let _real = null;
        const _queue = [];

        return {
            setReal(realInstance) {
                _real = realInstance;
                _ready = true;
                while (_queue.length) {
                    const item = _queue.shift();
                    try {
                        if (_real && typeof _real[item.fn] === 'function') {
                            _real[item.fn](...item.args);
                        }
                    } catch (err) {
                        console.error('Error ejecutando llamada encolada en settingsController:', err);
                    }
                }
            },
            saveSettings(...args) {
                if (_ready && _real && typeof _real.saveSettings === 'function') return _real.saveSettings(...args);
                _queue.push({ fn: 'saveSettings', args });
            },
            cancelChanges(...args) {
                if (_ready && _real && typeof _real.cancelChanges === 'function') return _real.cancelChanges(...args);
                _queue.push({ fn: 'cancelChanges', args });
            },
            submitApprovalRequest(...args) {
                if (_ready && _real && typeof _real.submitApprovalRequest === 'function') return _real.submitApprovalRequest(...args);
                _queue.push({ fn: 'submitApprovalRequest', args });
            },
            _isStub: true
        };
    })();
}

class DashboardInitializer {
    constructor() {
        // ✅ Prevenir múltiples instancias
        if (window.dashboardApp) {
            console.log('🛑 Dashboard ya inicializado, evitando duplicado');
            return window.dashboardApp;
        }

        this.endpoints = [
            'http://localhost:8081/api/admin/settings',
            'http://localhost:8081/api/internal/settings',
            'http://localhost:8081/api/internal/settings/simple',
            'http://localhost:8081/simple',
            'http://localhost:8081/health'
        ];

        this.isInitialized = false;
        this.init();
    }

    async init() {
        if (this.isInitialized) {
            console.log('🛑 Dashboard ya está inicializado');
            return;
        }

        console.log('🚀 Inicializando dashboard del Panel Local...');
        try {
            const data = await this.loadInitialData();
            this.updateDashboard(data);
            this.setupEventListeners();
            this.isInitialized = true;
            window.dashboardApp = this;
            console.log('✅ Dashboard inicializado correctamente');
        } catch (err) {
            console.error('❌ Error inicializando dashboard:', err);
            this.showInitError(err);
            window.dashboardApp = this;
        }
    }

    async loadInitialData() {
        console.log('📡 Cargando datos iniciales...');
        
        for (const url of this.endpoints) {
            try {
                console.log('🔍 Probando endpoint:', url);
                const resp = await fetch(url, {
                    method: 'GET',
                    headers: { 
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                });
                
                if (resp.status === 404) {
                    console.warn(`⚠️ 404 en ${url}, probando siguiente...`);
                    continue;
                }
                
                if (resp.status === 301 || resp.status === 302) {
                    console.warn(`⚠️ Redirect en ${url}, probando siguiente...`);
                    continue;
                }
                
                if (!resp.ok) {
                    const txt = await resp.text().catch(() => '');
                    console.warn(`⚠️ HTTP ${resp.status} en ${url} - ${txt.substring(0, 100)}`);
                    continue;
                }
                
                const json = await resp.json();
                console.log('✅ Datos obtenidos desde', url);
                console.log('📦 Datos:', json);
                return json;
            } catch (e) {
                console.warn(`ℹ️ Error al consultar ${url}:`, e.message || e);
            }
        }
        
        console.warn('⚠️ Ningún endpoint respondió, usando datos por defecto');
        return this.getDefaultData();
    }

    getDefaultData() {
        return {
            id: 'LOCAL_PARKING_001',
            name: 'Mi Estacionamiento',
            address: 'Ingrese la dirección de su estacionamiento',
            totalSpaces: 50,
            hourlyRate: 3.50,
            description: '',
            isVisible: false,
            registrationStatus: 'NOT_REGISTERED'
        };
    }

    updateDashboard(data) {
        try {
            if (!data) {
                console.warn('updateDashboard: data vacía');
                return;
            }
            
            console.log('📊 Actualizando dashboard con:', data);
            
            // Actualizar elementos del DOM de forma segura
            this.updateElement('stat-name', data.name);
            this.updateElement('parking-name', data.name);
            this.updateElement('total-spaces', data.totalSpaces);
            this.updateElement('hourly-rate', `S/ ${data.hourlyRate || '0.00'}`);
            this.updateRegistrationStatus(data.registrationStatus);
            
            console.log('✅ Dashboard actualizado correctamente');
        } catch (e) {
            console.error('❌ Error actualizando dashboard:', e);
        }
    }

    updateElement(elementId, value) {
        const element = document.getElementById(elementId);
        if (element && value !== undefined && value !== null) {
            element.textContent = value;
        }
    }

    updateRegistrationStatus(status) {
        const statusElement = document.getElementById('registration-status');
        if (!statusElement) return;
        
        const statusText = {
            'APPROVED': '✅ Aprobado',
            'PENDING': '⏳ Pendiente',
            'NOT_REGISTERED': '❌ No registrado',
            'REJECTED': '❌ Rechazado'
        };
        
        statusElement.textContent = statusText[status] || status;
    }

    setupEventListeners() {
        console.log('🔗 Configurando event listeners...');
        
        // Remover event listeners existentes primero
        this.removeExistingEventListeners();
        
        // Botón de refrescar
        const refreshBtn = document.getElementById('refresh-dashboard-btn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.handleRefresh());
            console.log('✅ Botón de refrescar configurado');
        }
        
        // Botón de guardar configuración
        const saveBtn = document.getElementById('save-settings-btn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.handleSaveSettings());
            console.log('✅ Botón de guardar configurado');
        }
    }

    removeExistingEventListeners() {
        // Clonar y reemplazar botones para eliminar listeners existentes
        const refreshBtn = document.getElementById('refresh-dashboard-btn');
        const saveBtn = document.getElementById('save-settings-btn');
        
        if (refreshBtn) {
            const newRefreshBtn = refreshBtn.cloneNode(true);
            refreshBtn.parentNode.replaceChild(newRefreshBtn, refreshBtn);
        }
        
        if (saveBtn) {
            const newSaveBtn = saveBtn.cloneNode(true);
            saveBtn.parentNode.replaceChild(newSaveBtn, saveBtn);
        }
    }

    async handleRefresh() {
        try {
            this.showTemporaryMessage('🔄 Refrescando datos...');
            const data = await this.loadInitialData();
            this.updateDashboard(data);
            this.showTemporaryMessage('✅ Datos actualizados', 2000);
        } catch (e) {
            console.error('❌ Error refrescando datos:', e);
            this.showTemporaryMessage('❌ Error al refrescar', 3000);
        }
    }

    handleSaveSettings() {
        if (window.settingsController && typeof window.settingsController.saveSettings === 'function') {
            window.settingsController.saveSettings();
        } else {
            console.warn('⚠️ settingsController.saveSettings no está disponible');
            this.showTemporaryMessage('⚠️ Controlador no disponible', 3000);
        }
    }

    showInitError(err) {
        const container = document.getElementById('dashboard-error');
        const message = typeof err === 'string' ? err : (err.message || String(err));
        
        if (container) {
            container.style.display = 'block';
            container.innerHTML = `
                <div style="padding: 15px; background: #fee; border: 1px solid #fcc; border-radius: 8px; margin: 10px 0;">
                    <strong>⚠️ Error cargando datos:</strong><br>
                    ${message}<br>
                    <small>Ver consola (F12) para detalles.</small>
                </div>
            `;
        }
    }

    showTemporaryMessage(msg, timeout = 3000) {
        let el = document.getElementById('dashboard-temp-msg');
        if (!el) {
            el = document.createElement('div');
            el.id = 'dashboard-temp-msg';
            el.style.cssText = `
                position: fixed;
                bottom: 20px;
                right: 20px;
                background: rgba(40, 167, 69, 0.95);
                color: #fff;
                padding: 12px 20px;
                border-radius: 8px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.2);
                z-index: 9999;
                font-size: 14px;
                font-weight: 500;
                transition: opacity 0.3s ease;
            `;
            document.body.appendChild(el);
        }
        
        el.textContent = msg;
        el.style.display = 'block';
        el.style.opacity = '1';
        
        clearTimeout(this._msgTimeout);
        this._msgTimeout = setTimeout(() => {
            el.style.opacity = '0';
            setTimeout(() => el.style.display = 'none', 300);
        }, timeout);
    }
}

// ✅ INICIALIZACIÓN SEGURA - Solo una vez
let dashboardInitialized = false;

function initializeDashboard() {
    if (dashboardInitialized) {
        console.log('🛑 Dashboard ya fue inicializado');
        return;
    }
    
    console.log('📄 DOM listo, inicializando DashboardInitializer...');
    try {
        new DashboardInitializer();
        dashboardInitialized = true;
    } catch (e) {
        console.error('❌ Error al iniciar DashboardInitializer:', e);
    }
}

// Esperar a que el DOM esté completamente listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeDashboard);
} else {
    // DOM ya está listo
    setTimeout(initializeDashboard, 100);
}

// Para debugging
window.reloadDashboard = async function() {
    if (window.dashboardApp) {
        const data = await window.dashboardApp.loadInitialData();
        window.dashboardApp.updateDashboard(data);
        console.log('✅ Dashboard recargado manualmente');
    } else {
        console.warn('⚠️ dashboardApp no está disponible');
    }
};

console.log('✅ dashboard-initializer.js cargado - versión corregida');