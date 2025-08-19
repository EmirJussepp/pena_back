<template>
  <div class="home-minimal">
    <!-- Hero -->
    <header class="hero">
      <img src="@/assets/logo_peña-removebg-preview.png" alt="Escudo Boca" class="logo" />
      <h1 class="title">¡Bienvenido<span v-if="displayName">, {{ displayName }}</span>!</h1>
    </header>

    <!-- Shortcuts -->
    <nav class="shortcuts" aria-label="Accesos rápidos">
      <RouterLink
        v-for="s in visibleShortcuts"
        :key="s.to"
        class="shortcut"
        :to="s.to"
        :title="s.title || s.label"
      >
        <span class="icon-wrap" aria-hidden="true">
          <i :class="s.icon" />
        </span>
        <span class="label">{{ s.label }}</span>
        <small v-if="s.hint" class="hint">{{ s.hint }}</small>
      </RouterLink>
    </nav>

    <p v-if="!visibleShortcuts.length" class="empty">
      No tenés accesos disponibles con tu rol. Consultá con un administrador.
    </p>

    <footer class="foot">© 2025 Peña Boquense</footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// --- JWT helpers ---
function parseJwt (t) {
  try {
    const b = t.split('.')[1]?.replace(/-/g,'+').replace(/_/g,'/') || ''
    return JSON.parse(decodeURIComponent(atob(b).split('').map(c => '%'+('00'+c.charCodeAt(0).toString(16)).slice(-2)).join('')))
  } catch { return {} }
}

const token = localStorage.getItem('token') || ''
const claims = token ? parseJwt(token) : {}
const perms = Array.isArray(claims?.perms) ? claims.perms : []

const displayName = computed(() => claims.name || claims.username || claims.email || '')

// --- permisos ---
function hasPerm (...need) {
  if (perms.includes('*')) return true
  return need.some(p => perms.includes(p))
}

// Definición de atajos (agregá/quità lo que necesites)
const shortcuts = [
  { label: 'Socios', icon: 'bi bi-people-fill', to: '/socios', hint: 'Listado y gestión', perms: ['socios:ver','socios:gestionar'] },
  { label: 'Cuotas', icon: 'bi bi-cash-coin', to: '/cuotas-pendientes', hint: 'Cobros pendientes', perms: ['cuotas:ver','cuotas:gestionar','socios:gestionar'] },
  { label: 'Viajes', icon: 'bi bi-bus-front-fill', to: '/viajes', hint: 'Ver viajes', perms: ['viajes:ver','viajes:gestionar'] },
  { label: 'Crear viaje', icon: 'bi bi-plus-circle-fill', to: '/viajesBombonera', hint: 'Nuevo viaje', perms: ['viajes:gestionar'] },
  { label: 'Calendario', icon: 'bi bi-calendar3', to: '/alquileres/calendario', hint: 'Salón / eventos', perms: ['alquileres:ver','alquileres:gestionar'] },
  { label: 'Movimientos', icon: 'bi bi-bar-chart-fill', to: '/movimientos', hint: 'Ingresos/Egresos', perms: ['movimientos:ver','movimientos:gestionar'] },
  { label: 'Beneficios', icon: 'bi bi-gift-fill', to: '/beneficios', hint: 'Descuentos y más', perms: ['beneficios:ver','beneficios:gestionar'] },
  { label: 'Cobradores', icon: 'bi bi-person-badge-fill', to: '/cobradores', hint: 'Administrar', perms: ['cobradores:ver','cobradores:gestionar','socios:gestionar'] },
  { label: 'Tipos Peña', icon: 'bi bi-people', to: '/sociospeña', hint: 'Configurar', perms: ['socios:gestionar'] },
  { label: 'Tipos Boca', icon: 'bi bi-trophy-fill', to: '/sociosboca', hint: 'Configurar', perms: ['socios:gestionar'] },
  { label: 'Salones', icon: 'bi bi-building', to: '/salones', hint: 'Espacios', perms: ['salones:ver','salones:gestionar'] },
  { label: 'Usuarios', icon: 'bi bi-person-gear', to: '/usuarios', hint: 'Permisos', perms: ['usuarios:gestionar'] }
]

const visibleShortcuts = computed(() => shortcuts.filter(s => hasPerm(...s.perms)))
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

.home-minimal { max-width: 1000px; margin: 48px auto; padding: 22px 24px 44px; display: flex; flex-direction: column; align-items: center; text-align: center; min-height: calc(100vh - 96px); color: #0f172a; }

/* Hero */
.hero { display: grid; place-items: center; gap: 8px; margin-bottom: 20px; user-select: none; }
.logo { width: 110px; filter: drop-shadow(0 2px 3px rgba(0,0,0,.15)); transition: transform .25s ease; }
.logo:hover { transform: rotate(-3deg) scale(1.06); }
.title { margin: 0; font-weight: 800; letter-spacing: .2px; font-size: clamp(1.6rem, 2.6vw, 2rem); color: #003366; }
.subtitle { margin: 0; color: #475569; font-weight: 600; }

/* Shortcuts grid */
/* ====== Grid: SIN CAMBIOS DE POSICIÓN ====== */
.shortcuts {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); /* intacto */
  gap: 16px;
  margin-top: 18px;
}

/* ====== Tarjeta atajo (Boca Pro) ====== */
.shortcut {
  /* layout intacto */
  display: grid;
  grid-template-rows: auto auto auto;
  justify-items: center;
  align-items: center;

  gap: 10px;
  padding: 22px 18px;
  min-height: 132px;
  text-align: center;
  text-decoration: none;
  font-weight: 800;

  /* estética */
  border-radius: 16px;
  border: 1px solid #dbe2f3;
  color: #0b3c5d;
  background:
    radial-gradient(1200px 400px at 10% -20%, rgba(255,193,7,.10) 0%, transparent 60%),
    linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);

  /* sombra más realista */
  box-shadow:
    0 1px 0 rgba(3, 18, 47, .04),
    0 8px 18px rgba(16,24,40,.06);

  /* transiciones suaves */
  transition:
    transform .18s ease,
    box-shadow .25s ease,
    border-color .25s ease,
    background .25s ease;
}

/* Hover: levita y toma acento CABJ */
.shortcut:hover {
  transform: translateY(-4px);
  border-color: #c7d2fe;
  background:
    radial-gradient(900px 320px at 85% -10%, rgba(0,51,102,.08) 0%, transparent 55%),
    linear-gradient(145deg, #ffffff 0%, #f3f6ff 100%);
  box-shadow:
    0 14px 28px rgba(16,24,40,.12),
    0 6px 12px rgba(16,24,40,.08);
}

/* Active: feedback inmediato */
.shortcut:active { transform: translateY(-2px) scale(.995); }

/* Foco accesible (tecla Tab) con colores CABJ */
.shortcut:focus-visible {
  outline: 3px solid rgba(255, 231, 17, 0.45);
  outline-offset: 2px;
  box-shadow:
    0 0 0 3px rgba(255,193,7,.25),
    0 10px 22px rgba(0,51,102,.18);
  border-color: #003366;
}

/* ====== Icono ====== */
.icon-wrap {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;

  border-radius: 14px;
  border: 1px solid #c7d2fe;
  background:
    linear-gradient(180deg, #eef2ff 0%, #e6ecff 100%),
    radial-gradient(circle at 70% 20%, rgba(255,255,255,.9) 0%, rgba(255,255,255,0) 40%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,.7),
    0 4px 10px rgba(0,51,102,.08);
  transition: transform .18s ease, box-shadow .25s ease;
}

.icon-wrap i,
.icon-wrap svg {
  font-size: 1.35rem;
  color: #003366;             /* azul CABJ */
  transition: transform .18s ease, color .2s ease;
}

/* micro-interacción en hover */
.shortcut:hover .icon-wrap { box-shadow:
  inset 0 1px 0 rgba(255,255,255,.8),
  0 8px 18px rgba(0,51,102,.14);
}
.shortcut:hover .icon-wrap i,
.shortcut:hover .icon-wrap svg {
  color: #173a72;
  transform: translateY(-1px) scale(1.06);
}

/* ====== Tipografías ====== */
.label { font-size: 1rem; color: #1c54ff; letter-spacing: .2px; }
.hint  { color: #64748b; font-weight: 600; }

/* Sello inferior/estado */
.foot  { margin-top: auto; color: #94a3b8; font-size: .92rem; }

/* Estado vacío */
.empty { margin-top: 22px; color: #64748b; font-weight: 600; }

/* Responsivo: SIN tocar el layout base */
@media (max-width: 520px) {
  .shortcuts { grid-template-columns: 1fr 1fr; } /* igual que tenías */
}

/* Respeto a usuarios con reducción de motion */
@media (prefers-reduced-motion: reduce) {
  .shortcut,
  .icon-wrap,
  .icon-wrap i,
  .icon-wrap svg {
    transition: none;
  }
}

</style>
