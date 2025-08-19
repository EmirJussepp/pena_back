<template>
  <div class="layout" :class="{ 'sidebar-collapsed': isCollapsed }">
    <!-- Sidebar -->
    <aside :class="['sidebar', { collapsed: isCollapsed }]" @keydown.esc="closeAllSubmenus">
      <!-- Header -->
      <header class="sidebar-header">
        <button class="toggle-btn" @click="toggleSidebar" :aria-expanded="!isCollapsed" aria-controls="nav-root"
          title="Alternar menú">
          <i class="bi bi-list"></i>
        </button>
        <div v-if="!isCollapsed && displayName" class="user-name">
          Hola, {{ displayName.nombre }}
        </div>
      </header>

      <!-- Navigation -->
      <nav id="nav-root" class="nav-links" role="navigation" aria-label="Navegación principal">
        <RouterLink :class="linkClass('/principal')" to="/principal">
          <i class="bi bi-house-door-fill"></i>
          <span v-if="!isCollapsed">Inicio</span>
        </RouterLink>

        <!-- Socios -->
        <div class="nav-group">
          <button class="nav-link submenu-toggle" :aria-expanded="submenu.socios" @click="toggleSubmenu('socios')"
            :aria-controls="'submenu-socios'">
            <i class="bi bi-people-fill"></i>
            <span v-if="!isCollapsed">Socios</span>
            <i v-if="!isCollapsed" class="bi ms-auto" :class="submenu.socios ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </button>
          <transition name="slide-fade">
            <div v-show="submenu.socios && !isCollapsed" id="submenu-socios" class="submenu-items">
              <RouterLink :class="sublinkClass('/socios')" to="/socios">
                <i class="bi bi-person-lines-fill me-2"></i> Activos
              </RouterLink>
              <RouterLink :class="sublinkClass('/socios-baja')" to="/socios-baja">
                <i class="bi bi-person-dash-fill me-2"></i> Dados de baja
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- Cuotas -->
        <div class="nav-group">
          <button class="nav-link submenu-toggle" :aria-expanded="submenu.cuotas" @click="toggleSubmenu('cuotas')"
            aria-controls="submenu-cuotas">
            <i class="bi bi-cash-coin"></i>
            <span v-if="!isCollapsed">Cuotas</span>
            <i v-if="!isCollapsed" class="bi ms-auto" :class="submenu.cuotas ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </button>
          <transition name="slide-fade">
            <div v-show="submenu.cuotas && !isCollapsed" id="submenu-cuotas" class="submenu-items">
              <RouterLink :class="sublinkClass('/cuotas-pendientes')" to="/cuotas-pendientes">
                <i class="bi bi-hourglass-split me-2"></i> Pendientes
                <span v-if="badges.cuotasPendientes > 0" class="badge">
                  {{ formatBadge(badges.cuotasPendientes) }}
                </span>
              </RouterLink>
              <RouterLink :class="sublinkClass('/cuotas-cobrador')" to="/cuotas-cobrador">
                <i class="bi bi-person-badge-fill me-2"></i> Por cobrador
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- Movimientos -->
        <RouterLink :class="linkClass('/movimientos')" to="/movimientos">
          <i class="bi bi-bar-chart-fill"></i>
          <span v-if="!isCollapsed">Movimientos</span>
        </RouterLink>

        <!-- Beneficios -->
        <RouterLink :class="linkClass('/beneficios')" to="/beneficios">
          <i class="bi bi-gift-fill"></i>
          <span v-if="!isCollapsed">Beneficios</span>
        </RouterLink>

        <!-- Viajes -->
        <div class="nav-group">
          <button class="nav-link submenu-toggle" :aria-expanded="submenu.viajes" @click="toggleSubmenu('viajes')"
            aria-controls="submenu-viajes">
            <i class="bi bi-bus-front-fill"></i>
            <span v-if="!isCollapsed">Viajes</span>
            <i v-if="!isCollapsed" class="bi ms-auto" :class="submenu.viajes ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </button>
          <transition name="slide-fade">
            <div v-show="submenu.viajes && !isCollapsed" id="submenu-viajes" class="submenu-items">
              <RouterLink :class="sublinkClass('/viajesBombonera')" to="/viajesBombonera">
                <i class="bi bi-plus-circle-fill me-2"></i> Crear viaje
              </RouterLink>
              <RouterLink :class="sublinkClass('/viajes')" to="/viajes">
                <i class="bi bi-journal-text me-2"></i> Ver viajes
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- Alquileres -->
        <div class="nav-group">
          <button class="nav-link submenu-toggle" :aria-expanded="submenu.alquileres"
            @click="toggleSubmenu('alquileres')" aria-controls="submenu-alquileres">
            <i class="bi bi-calendar2-event-fill"></i>
            <span v-if="!isCollapsed">Alquileres</span>
            <i v-if="!isCollapsed" class="bi ms-auto"
              :class="submenu.alquileres ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </button>
          <transition name="slide-fade">
            <div v-show="submenu.alquileres && !isCollapsed" id="submenu-alquileres" class="submenu-items">
              <RouterLink :class="sublinkClass('/alquileres/calendario')" to="/alquileres/calendario">
                <i class="bi bi-calendar3 me-2"></i> Calendario
              </RouterLink>
            </div>
          </transition>
        </div>
      </nav>

      <!-- Footer -->
      <footer :class="['sidebar-footer', { collapsed: isCollapsed }]">
        <button class="config-btn" v-if="canManageConfig" title="Configuración" @click="mostrarConfig = true">
          <i class="bi bi-gear-fill"></i>
        </button>
        <button class="logout-btn" title="Cerrar sesión" @click="logout">
          <i class="bi bi-box-arrow-right"></i>
        </button>
      </footer>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Modal Configuración -->
    <div v-if="mostrarConfig" class="modal-fondo" @click.self="mostrarConfig = false">
      <section class="modal-contenido" role="dialog" aria-modal="true" aria-labelledby="config-title">
        <h5 id="config-title">Menú de Configuración</h5>
        <div v-for="(categoria, index) in categorias" :key="index" class="categoria">
          <button class="categoria-header" @click="toggleCategoria(index)" :aria-expanded="categoria.abierto">
            <strong>{{ categoria.nombre }}</strong>
            <i :class="categoria.abierto ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
          </button>
          <ul v-show="categoria.abierto" class="opciones-lista">
            <li v-for="opcion in categoria.opciones" :key="opcion.nombre">
              <RouterLink :to="opcion.ruta" class="opcion-link" @click="mostrarConfig = false">
                {{ opcion.nombre }}
              </RouterLink>
            </li>
          </ul>
        </div>
        <button class="btn btn-sm btn-secondary mt-3" @click="mostrarConfig = false">Cerrar</button>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// --- Axios con token ---
const api = axios.create({ baseURL: "http://127.0.0.1:8081" })
api.interceptors.request.use((config) => {
  const t = localStorage.getItem("token")
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})

// --- Estado ---
const token = ref(localStorage.getItem("token") || "")

const isCollapsed = ref(JSON.parse(localStorage.getItem('sidebarCollapsed') || 'false'))
const mostrarConfig = ref(false)
const submenu = reactive({ socios: false, cuotas: false, viajes: false, alquileres: false })
const badges = reactive({ cuotasPendientes: 0 })

// --- Permisos ---
function parseJwt(t) {
  try {
    const base64 = t.split(".")[1]?.replace(/-/g, "+").replace(/_/g, "/") || ""
    return JSON.parse(decodeURIComponent(atob(base64).split("").map(c =>
      "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2)
    ).join("")))
  } catch { return {} }
}

const claims = computed(() => token.value ? parseJwt(token.value) : {})
const perms = computed(() => claims.value?.perms || [])
const canManageConfig = computed(() =>
  perms.value.includes("*") || perms.value.includes("usuarios:gestionar")
)
const displayName = computed(() => {
  const c = claims.value || {}
  return c.nombre || c.name || c.email || ''
})


// --- Configuración modal ---
const categorias = reactive([
  { nombre: 'Salones', abierto: false, opciones: [{ nombre: 'Actualizar Salones', ruta: '/salones' }] },
  {
    nombre: 'Tipos de Socios', abierto: false, opciones: [
      { nombre: 'Actualizar Socios de la Peña', ruta: '/sociospeña' },
      { nombre: 'Actualizar Socios de Boca', ruta: '/sociosboca' }
    ]
  },
  { nombre: 'Cobradores', abierto: false, opciones: [{ nombre: 'Actualizar Cobradores', ruta: '/cobradores' }] }
])

// --- Métodos ---
function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebarCollapsed', JSON.stringify(isCollapsed.value))
  if (isCollapsed.value) closeAllSubmenus()
}

function toggleSubmenu(key) {
  Object.keys(submenu).forEach(k => { if (k !== key) submenu[k] = false })
  submenu[key] = !submenu[key]
}

function closeAllSubmenus() { Object.keys(submenu).forEach(k => submenu[k] = false) }
function toggleCategoria(index) { categorias[index].abierto = !categorias[index].abierto }
function logout() { localStorage.removeItem('token'); router.push('/login') }

function linkClass(path) { return ['nav-link', { active: route.path === path || route.path.startsWith(path + '/') }] }
function sublinkClass(path) { return ['submenu-link', { active: route.path === path || route.path.startsWith(path + '/') }] }

function formatBadge(n) {
  if (!n) return ''
  if (n < 1000) return String(n)
  if (n < 10000) return (n / 1000).toFixed(1) + 'k'
  return Math.round(n / 1000) + 'k'
}

function syncOpenSubmenuWithRoute() {
  const p = route.path
  submenu.socios = /^\/socios(\b|\/)/.test(p) || /^\/socios-baja/.test(p)
  submenu.cuotas = /^\/cuotas(\b|\/)/.test(p)
  submenu.viajes = /^\/viajes(\b|\/)/.test(p) || /^\/viajesBombonera/.test(p)
  submenu.alquileres = /^\/alquileres(\b|\/)/.test(p)
}

watch(() => route.path, () => syncOpenSubmenuWithRoute())
onMounted(() => syncOpenSubmenuWithRoute())
</script>

<style scoped>
/* Layout */
.layout {
  display: flex;
  height: 100vh;
  background-color: #f6f7fb;
}

.sidebar {
  background-color: #111827;
  width: 260px;
  min-width: 260px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: width .25s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, .2);
}

.sidebar.collapsed {
  width: 80px;
  min-width: 80px;
}

/* Header */
.sidebar-header {
  display: flex;
  align-items: center;
  gap: .75rem;
  padding: .9rem .9rem;
  border-bottom: 1px solid rgba(255, 255, 255, .06);
}

.toggle-btn {
  background: transparent;
  border: none;
  color: #facc15;
  font-size: 1.5rem;
  cursor: pointer;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  color: #e5e7eb;
  text-decoration: none;
  font-weight: 700;
  letter-spacing: .3px;
}

.brand:hover {
  color: #fff;
}

.brand-text {
  font-size: 1.05rem;
}

/* Nav */
.nav-links {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: .5rem;
  overflow-y: auto;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: .75rem .9rem;
  border-radius: .75rem;
  color: #cbd5e1;
  text-decoration: none;
  transition: background .18s ease, color .18s ease;
}

.nav-link:hover {
  background-color: #1f2937;
  color: #fff;
}

.nav-link.active {
  background: linear-gradient(90deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
  box-shadow: 0 4px 18px rgba(37, 99, 235, .35);
}

.nav-group {
  display: flex;
  flex-direction: column;
}

.submenu-toggle {
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
}

.submenu-items {
  display: flex;
  flex-direction: column;
  padding-left: 2.2rem;
  gap: 2px;
  margin: 2px 0 8px;
}

.submenu-link {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  padding: .45rem .75rem;
  border-radius: .5rem;
  color: #94a3b8;
  text-decoration: none;
  position: relative;
}

.submenu-link:hover {
  background-color: #1f2937;
  color: #fff;
}

.submenu-link.active {
  color: #fff;
  font-weight: 600;
}

.badge {
  margin-left: .5rem;
  padding: .05rem .4rem;
  border-radius: .5rem;
  background: #ef4444;
  color: #fff;
  font-size: .72rem;
  font-weight: 700;
}

/* Footer */
.sidebar-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: .8rem .9rem;
  border-top: 1px solid rgba(255, 255, 255, .06);
}

.sidebar-footer.collapsed {
  flex-direction: column;
  gap: .6rem;
}

.config-btn,
.logout-btn {
  background: transparent;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
}

.config-btn {
  color: #facc15;
}

.config-btn:hover {
  color: #fde047;
}

.logout-btn {
  color: #f87171;
}

.logout-btn:hover {
  color: #ef4444;
}

/* Main */
.main-content {
  flex: 1;
  padding: 1.1rem;
  overflow-y: auto;
}

/* Modal */
.modal-fondo {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  display: grid;
  place-items: center;
  z-index: 50;
}

.modal-contenido {
  background: #fff;
  padding: 1.6rem;
  border-radius: 12px;
  width: min(92vw, 420px);
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, .25);
  border: 2px solid #003366;
}

.modal-contenido h5 {
  font-size: 1.25rem;
  font-weight: 800;
  color: #003366;
  text-align: center;
  margin-bottom: 1rem;
  position: relative;
}

.modal-contenido h5::after {
  content: "";
  display: block;
  width: 120px;
  height: 4px;
  background: #ffc107;
  margin: .25rem auto 0;
  border-radius: 2px;
}

.categoria {
  margin-bottom: 1rem;
  padding-bottom: .5rem;
  border-bottom: 1px solid #e5e7eb;
}

.categoria-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #003366;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  padding: .25rem 0;
  background: transparent;
  border: none;
}

.opciones-lista {
  list-style: none;
  padding-left: .75rem;
  margin-top: .5rem;
}

.opcion-link {
  display: block;
  padding: .45rem .75rem;
  color: #374151;
  border-radius: 6px;
  text-decoration: none;
}

.opcion-link:hover {
  background: #e0e7ff;
  color: #1e3a8a;
}

/* Transitions */
.slide-fade-enter-active {
  transition: all .2s ease;
}

.slide-fade-leave-active {
  transition: all .15s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.user-name {
  font-weight: bold;
  font-size: 0.9rem;
  color: #fff;
}

/* Responsive: auto-colapse en pantallas chicas */
@media (max-width: 1024px) {
  .sidebar {
    position: sticky;
    top: 0;
    height: 100vh;
  }
}
</style>
