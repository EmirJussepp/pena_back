<template>
  <div class="page-center">
    <div class="cuotas-pendientes">
      <!-- Header -->
      <header class="header">
        <h2 class="titulo-reporte">Informe de Cuotas Pendientes</h2>
        <p class="sub">Consultá el estado de cuotas por DNI y gestioná el cobro de manera simple.</p>
      </header>

      <!-- BUSCADOR -->
      <form @submit.prevent="buscarCuotas" class="buscador" role="search" aria-label="Buscar cuotas por DNI">
        <div class="input-wrap">
          <span class="prefix">DNI</span>
          <input
            ref="dniInput"
            v-model="dni"
            type="text"
            inputmode="numeric"
            placeholder="Ej. 32123456"
            required
            maxlength="10"
            pattern="[0-9]+"
            title="Sólo números"
            autocomplete="off"
            @input="soloNumeros(); validarDni()"
            @keydown.esc="limpiar()"
          />
          <button v-if="dni" class="btn-clear" type="button" @click="limpiar" aria-label="Limpiar búsqueda">
            <i class="bi bi-x-lg" aria-hidden="true"></i>
          </button>
          <button class="btn-buscar" type="submit" :disabled="cargando || !dniValido">
            <span v-if="cargando" class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            <span v-else>Buscar</span>
          </button>
        </div>
        <small class="ayuda" :class="{ invalid: !dniValido && dni }">
          {{ dniAyuda }}
        </small>
      </form>

      <!-- Mensajes -->
      <div v-if="cargando" class="alert cargando" role="status">Buscando cuotas…</div>

      <!-- DATOS DEL SOCIO -->
      <section v-if="socio" class="datos-socio" aria-labelledby="datos-socio-title">
        <h3 id="datos-socio-title">👤 Datos del Socio</h3>
        <div class="grid">
          <div class="dato"><span>Nombre</span><strong>{{ socio.nombre }} {{ socio.apellido }}</strong></div>
          <div class="dato"><span>Email</span><strong>{{ socio.email || 'No registrado' }}</strong></div>
          <div class="dato"><span>Teléfono</span><strong>{{ socio.telefono || 'No registrado' }}</strong></div>
          <div class="dato"><span>Dirección</span><strong class="truncate">{{ socio.direccion || 'No registrada' }}</strong></div>
          <div class="dato"><span>Fecha de ingreso</span><strong>{{ socio.fechaInicio ? new Date(socio.fechaInicio).toLocaleDateString('es-AR') : 'No disponible' }}</strong></div>
        </div>
      </section>

      <!-- RESULTADOS -->
      <section v-if="cuotas.length > 0" class="resultado" aria-live="polite">
        <div class="resultado-header">
          <h3>Cuotas Pendientes <span class="badge">{{ cuotas.length }}</span></h3>
          <p class="total-adeudado"><strong>Total adeudado:</strong> $ {{ Number(totalAdeudado).toFixed(2) }}</p>
        </div>

        <!-- Tabla -->
        <div class="table-wrapper" role="table" aria-label="Listado de cuotas">
          <table>
            <thead>
              <tr>
                <th class="seleccionar-th">
                  <label class="select-all" title="Seleccionar todas las pendientes">
                    <input
                      type="checkbox"
                      :checked="allSelected"
                      @change="toggleSelectAll($event.target.checked)"
                      aria-label="Seleccionar todas las cuotas pendientes"
                    />
                    <span>Seleccionar</span>
                  </label>
                </th>
                <th>Monto</th>
                <th>Periodo</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="cuota in cuotas"
                :key="cuota.cuotaId"
                :class="isPaid(cuota) ? 'pagado-row' : 'pendiente-row'"
              >
                <td class="text-center">
                  <input
                    type="checkbox"
                    v-model="cuotasSeleccionadas"
                    :value="cuota"
                    :disabled="isPaid(cuota)"
                    aria-label="Seleccionar cuota"
                  />
                </td>
                <td class="monto">$ {{ formatoMoneda(cuota.monto) }}</td>
                <td>{{ obtenerNombreMes(cuota.fechaPago) }}</td>
                <td>
                  <span :class="isPaid(cuota) ? 'badge-estado pagado' : 'badge-estado pendiente'">
                    {{ isPaid(cuota) ? 'Pagado' : 'Pendiente' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Forma de pago -->
        <div class="metodo-pago">
          <span class="label">Forma de pago:</span>
          <div class="metodos">
            <template v-for="m in metodosPago" :key="m.metodoPagoId">
              <input
                class="radio-visually-hidden"
                type="radio"
                :id="'metodo-'+m.metodoPagoId"
                name="metodo"
                :value="m.metodoPagoId"
                v-model="metodoPagoId"
              />
              <label :for="'metodo-'+m.metodoPagoId" class="metodo-card" :title="m.nombre">
                <i :class="iconoMetodo(m.nombre)" aria-hidden="true"></i>
                <span>{{ m.nombre }}</span>
              </label>
            </template>
          </div>
        </div>

        <!-- Resumen flotante -->
        <div class="resumen-pago" :class="{ visible: cuotasSeleccionadas.length > 0 }" role="region" aria-label="Resumen de pago">
          <div class="stats">
            <span><strong>Seleccionadas:</strong> {{ cuotasSeleccionadas.length }}</span>
            <span><strong>Total a pagar:</strong> $ {{ totalSeleccionado }}</span>
          </div>
          <div class="acciones">
            <button class="btn-outline" @click="seleccionarTodoPendiente" :disabled="elegibles.length===0">Seleccionar todo</button>
            <button class="btn-sec" @click="pagarTodo" :disabled="elegibles.length===0">Pagar todo</button>
            <button class="btn-prim" @click="pagarCuotas" :disabled="cuotasSeleccionadas.length===0">
              💳 Pagar seleccionadas
            </button>
          </div>
        </div>

        <div class="acciones-secundarias">
          <button @click="imprimir" class="btn-imprimir">🖨️ Imprimir Reporte</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import Swal from 'sweetalert2'
import axios from 'axios'

/** Axios con interceptores */
const api = axios.create({ baseURL: 'http://127.0.0.1:8081' })
api.interceptors.request.use((config) => {
  const t = localStorage.getItem('token')
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const s = err.response?.status
    if (s === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else if (s === 403) {
      console.warn('No autorizado para esta acción.')
    }
    return Promise.reject(err)
  }
)

/** State */
const dni = ref('')
const dniInput = ref(null)
const dniValido = ref(false)
const dniAyuda = ref('Ingresá sólo números, 7 a 10 dígitos.')

const cuotas = ref([])
const totalAdeudado = ref('0')
const error = ref(null)
const cargando = ref(false)
const buscado = ref(false)
const socio = ref(null)

const cuotasSeleccionadas = ref([])
const metodoPagoId = ref(null)
const metodosPago = ref([])

const metodoRequerido = ref(false) // <- flag de alerta visual

/** Carga métodos de pago */
onMounted(async () => {
  try {
    const { data } = await api.get('/metodopago')
    metodosPago.value = data || []
  } catch (e) {
    console.error('Error al cargar métodos de pago:', e)
    Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudieron cargar los métodos de pago.', confirmButtonColor: '#003366' })
  }
  await nextTick()
  dniInput.value?.focus()
})

/** Helpers */
const formatoMoneda = (num) => {
  if (isNaN(num)) return '0,00'
  return new Intl.NumberFormat('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num)
}
const isPaid = (cuota) => cuota?.estado === true || cuota?.estado === 'true'
function obtenerNombreMes(fechaString) {
  const fecha = new Date(fechaString)
  const mes = fecha.toLocaleDateString('es-AR', { month: 'long' }).toUpperCase()
  const anio = fecha.getFullYear()
  return `${mes} ${anio}`
}
function soloNumeros() {
  dni.value = (dni.value || '').replace(/\D+/g, '').slice(0, 10)
}
function validarDni() {
  const l = (dni.value || '').length
  dniValido.value = l >= 7 && l <= 10
  dniAyuda.value = dniValido.value ? 'Presioná Enter para buscar.' : 'Ingresá sólo números, 7 a 10 dígitos.'
}
function limpiar() {
  dni.value = ''
  validarDni()
  nextTick(() => dniInput.value?.focus())
}

/** Select-all logic (solo cuotas pendientes) */
const elegibles = computed(() => cuotas.value.filter((c) => !isPaid(c)))
const allSelected = computed(() => elegibles.value.length > 0 && cuotasSeleccionadas.value.length === elegibles.value.length)
function toggleSelectAll(checked) { cuotasSeleccionadas.value = checked ? elegibles.value.slice() : [] }
function seleccionarTodoPendiente() { toggleSelectAll(true) }

/** Guard de método requerido */
function exigirMetodoPago() {
  if (!metodoPagoId.value) {
    metodoRequerido.value = true
    Swal.fire({
      icon: 'warning',
      title: 'Seleccioná un método de pago',
      text: 'Para continuar con el cobro tenés que elegir una forma de pago.',
      confirmButtonColor: '#003366'
    })
    setTimeout(() => {
      document.querySelector('.metodo-pago')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }, 50)
    return true // falta método
  }
  return false
}

/** Buscar cuotas por DNI */
async function buscarCuotas() {
  error.value = null
  cuotas.value = []
  cuotasSeleccionadas.value = []
  totalAdeudado.value = '0'
  buscado.value = false
  socio.value = null
  metodoPagoId.value = null
  metodoRequerido.value = false

  if (!dniValido.value) {
    Swal.fire({ icon: 'warning', title: 'Atención', text: 'Ingresá un DNI válido (7 a 10 dígitos).', confirmButtonColor: '#003366' })
    return
  }

  cargando.value = true
  try {
    const { data } = await api.get('/cuotas/pendientes', { params: { dni: dni.value.trim() } })
    cuotas.value = (data?.cuotasPendientes || []).map((cuota) => ({ ...cuota, monto: Number(cuota.monto) || 0 }))
    totalAdeudado.value = data?.totalAdeudado ?? '0'
    buscado.value = true

    if (cuotas.value.length > 0) {
      const socioId = cuotas.value[0].socioId
      const { data: socioData } = await api.get(`/socios/${socioId}`)
      socio.value = socioData
    }
  } catch (err) {
    error.value = err.response?.data?.error || 'No se pudo conectar con el servidor.'
    Swal.fire({ icon: 'error', title: 'Error', text: error.value, confirmButtonColor: '#003366' })
  } finally {
    cargando.value = false
  }
}

/** Total seleccionado */
const totalSeleccionado = computed(() =>
  cuotasSeleccionadas.value.reduce((suma, cuota) => suma + (Number(cuota.monto) || 0), 0).toFixed(2)
)

/** Método de pago: iconos por nombre */
function iconoMetodo(nombre = '') {
  const n = nombre.toLowerCase()
  if (n.includes('efectivo')) return 'bi bi-cash-coin'
  if (n.includes('transfer')) return 'bi bi-bank'
  if (n.includes('mercado') || n.includes('mp')) return 'bi bi-qr-code-scan'
  if (n.includes('tarjeta') || n.includes('card')) return 'bi bi-credit-card-2-front'
  return 'bi bi-wallet2'
}

/** Pagar seleccionadas */
async function pagarCuotas() {
  // 1) No hay cuotas cargadas aún
  if (!Array.isArray(cuotas.value) || cuotas.value.length === 0) {
    return Swal.fire({
      icon: 'info',
      title: 'Sin resultados',
      text: 'Buscá primero un socio por DNI para ver sus cuotas.',
      confirmButtonColor: '#003366'
    })
  }

  // 2) El socio no tiene pendientes (todas pagas)
  if (elegibles.value.length === 0) {
    return Swal.fire({
      icon: 'info',
      title: 'Todo al día',
      text: 'El socio no tiene cuotas pendientes.',
      confirmButtonColor: '#003366'
    })
  }

  // 3) No seleccionaste ninguna cuota
  if (cuotasSeleccionadas.value.length === 0) {
    return Swal.fire({
      icon: 'warning',
      title: 'Atención',
      text: 'Seleccioná al menos una cuota.',
      confirmButtonColor: '#003366'
    })
  }

  // 4) Falta método de pago (muestra alerta y hace scroll)
  if (typeof exigirMetodoPago === 'function' && exigirMetodoPago()) return

  // 5) Validación de socio
  const socioIdNum = Number(socio.value?.socioId)
  if (!socioIdNum) {
    return Swal.fire({
      icon: 'warning',
      title: 'Atención',
      text: 'No se encontró el socio.',
      confirmButtonColor: '#003366'
    })
  }

  // 6) Validación de método de pago como número
  const metodoId = Number(metodoPagoId.value)
  if (!Number.isInteger(metodoId)) {
    return Swal.fire({
      icon: 'warning',
      title: 'Falta método de pago',
      text: 'Seleccioná un método de pago para continuar.',
      confirmButtonColor: '#003366'
    })
  }

  // 7) Construcción del payload
  const cuotasIds = cuotasSeleccionadas.value.map(c => c.cuotaId)
  const payload = {
    socioId: socioIdNum,
    cuotaId: cuotasIds,
    metodoPagoId: metodoId
  }

  try {
    await api.post('/pagos', payload)
    await Swal.fire({
      icon: 'success',
      title: 'Pago realizado',
      text: 'Pago realizado exitosamente.',
      confirmButtonColor: '#003366'
    })
    cuotasSeleccionadas.value = []
    if (typeof metodoRequerido !== 'undefined') metodoRequerido.value = false
    await buscarCuotas()
  } catch (error) {
    console.error('Error al pagar cuotas:', error)
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: error.response?.data?.error || 'Hubo un error al realizar el pago.',
      confirmButtonColor: '#003366'
    })
  }
}

/** Pagar todo (atajo) */
async function pagarTodo() {
  if (elegibles.value.length === 0) {
    return Swal.fire({ icon: 'info', title: 'Sin cuotas pendientes', text: 'No hay cuotas pendientes para pagar.', confirmButtonColor: '#003366' })
  }
  if (exigirMetodoPago()) return
  cuotasSeleccionadas.value = elegibles.value.slice()
  await pagarCuotas()
}

/** Imprimir */
function imprimir() { window.print() }

/** Accesos rápidos */
window.addEventListener('keydown', (e) => {
  if (e.ctrlKey && e.key.toLowerCase() === 'a') { e.preventDefault(); seleccionarTodoPendiente() }
})
</script>

<style scoped>
/* Centrado vertical/horizontal del módulo */
.page-center {
  min-height: 100vh;
  display: flex;
  align-items: center;       /* vertical */
  justify-content: center;   /* horizontal */
  padding: 2rem 1rem;
  background: linear-gradient(180deg, #f7faff 0%, #ffffff 100%);
}

/* Caja principal */
.cuotas-pendientes {
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
  background: #ffffff;
  padding: 1.5rem 1.75rem 5rem;
  border-radius: 16px;
  box-shadow: 0 10px 28px rgba(0,0,0,.07);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #0b3c5d;
}

/* Header */
.header { text-align: center; margin-bottom: .5rem; }
.titulo-reporte {
  color: #003366; font-weight: 900; letter-spacing: .02em; margin: 0;
  position: relative; display: inline-block;
}
.titulo-reporte::after {
  content: ""; display: block; width: 160px; height: 4px; background: #ffc107;
  margin: .35rem auto 0; border-radius: 2px;
}
.sub { color: #526278; margin-top: .35rem; font-size: .95rem; }

/* Buscador (simple y responsivo) */
.buscador { display: flex; flex-direction: column; align-items: center; gap: .35rem; margin: 1rem 0 1.25rem; width: 100%; }
.input-wrap {
  display: flex; align-items: center; gap: .5rem;
  border: 1.5px solid #00529F; border-radius: 12px; padding: .4rem .5rem;
  background: #fdfdff;
  width: 100%;
  max-width: 520px; /* prolijo en desktop */
}
.prefix { padding: .4rem .6rem; background: #e9f3ff; color: #003366; border-radius: 8px; font-weight: 800; }
.input-wrap input {
  flex: 1; border: none; outline: none; padding: .5rem .25rem; font-size: 1rem; color: #003366; background: transparent;
}
.input-wrap:focus-within { box-shadow: 0 0 0 3px rgba(255,193,7,.25); border-color: #ffc107; }
.btn-buscar {
  background: #003366; color: #ffc107; border: none; padding: .5rem .9rem; border-radius: 10px; font-weight: 800; cursor: pointer;
}
.btn-buscar:hover:not(:disabled) { background: #013C8A; color: #fff; }
.btn-buscar:disabled { opacity: .6; cursor: not-allowed; }
.btn-clear {
  background: #fff; border: 1px solid #e5e7eb; color: #111827; padding: .4rem .6rem; border-radius: 8px; cursor: pointer;
}
.btn-clear:hover { background: #f3f4f6; }
.ayuda { color: #6b7280; font-size: .9rem; }
.ayuda.invalid { color: #a11328; }

/* Alerts */
.alert { padding: .75rem 1rem; border-radius: 10px; margin: .5rem 0; }
.error { background: #fdecec; color: #8b1a1a; border: 1px solid #f8d7da; }
.cargando { background: #eef6ff; color: #0b3c5d; border: 1px solid #dbeafe; }
.alert.aviso {
  background: #fff9e6;
  border: 1px solid #ffe29a;
  color: #734d00;
  border-radius: 10px;
  padding: .65rem .8rem;
  margin-top: .5rem;
}

/* Socio */
.datos-socio {
  background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 12px; padding: 1rem 1.25rem; margin: 1rem 0 1.25rem;
}
.datos-socio h3 { margin: 0 0 .6rem; color: #003366; font-weight: 800; }
.datos-socio .grid {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .5rem 1rem;
}
.dato span { display: block; font-size: .8rem; color: #6b7280; }
.dato strong { font-size: 1.02rem; color: #0b3c5d; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Result header */
.resultado .resultado-header {
  display: flex; align-items: baseline; justify-content: space-between; gap: 1rem; flex-wrap: wrap;
}
.resultado h3 { color: #003366; font-weight: 900; margin: .2rem 0; display: flex; align-items: center; gap: .5rem; }
.badge { background: #eef4ff; color: #173a72; border: 1px solid #dbe6ff; padding: .15rem .5rem; border-radius: .5rem; font-weight: 800; }
.total-adeudado { margin: 0 0 .75rem; color: #0b3c5d; }

/* Tabla */
.table-wrapper { border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
thead { background: #f2f7ff; border-bottom: 2px solid #00529F; position: sticky; top: 0; z-index: 1; }
th, td { padding: .65rem .75rem; border-bottom: 1px solid #eef2f8; vertical-align: middle; }
tr:hover { background: #f9fbff; }
.monto { font-weight: 800; color: #0b3c5d; }
.seleccionar-th { width: 160px; }
.select-all { display: inline-flex; align-items: center; gap: .45rem; user-select: none; font-weight: 800; color: #003366; }

.badge-estado { padding: .2rem .6rem; border-radius: .5rem; font-weight: 800; border: 1px solid transparent; }
.badge-estado.pagado { background: #e7f6ec; color: #136f3a; border-color: #cfeedd; }
.badge-estado.pendiente { background: #fdecec; color: #a11328; border-color: #f7d0d6; }
.pagado-row { background: #fbfdfc; }
.pendiente-row { background: #fff; }

/* Métodos de pago (segmented) */
.metodo-pago { margin: 1rem 0 .5rem; display: flex; align-items: center; gap: .75rem; flex-wrap: wrap; }
.metodo-pago .label { font-weight: 800; color: #003366; }
.metodos { display: flex; gap: .5rem; flex-wrap: wrap; }
.radio-visually-hidden { position: absolute; opacity: 0; width: 0; height: 0; }
.metodo-card {
  display: inline-flex; align-items: center; gap: .45rem;
  padding: .5rem .7rem; border: 1.5px solid #dbe6ff; border-radius: 10px; cursor: pointer;
  background: #fff; color: #173a72; font-weight: 800; user-select: none;
}
.metodo-card i { font-size: 1.05rem; }
.radio-visually-hidden:checked + .metodo-card {
  border-color: #00529F; background: #f2f7ff; box-shadow: 0 0 0 3px rgba(255,193,7,.2);
}

/* Resumen de pago (barra flotante) */
.resumen-pago {
  position: sticky; bottom: 0; left: 0; right: 0;
  display: flex; justify-content: space-between; align-items: center; gap: .75rem;
  background: rgba(255,255,255,.9); backdrop-filter: blur(6px);
  border: 1px solid #e5e7eb; border-radius: 12px; padding: .6rem .8rem; margin-top: 1rem;
  opacity: 0; pointer-events: none; transform: translateY(6px); transition: all .2s ease;
}
.resumen-pago.visible { opacity: 1; pointer-events: auto; transform: translateY(0); }
.resumen-pago .stats { display: flex; gap: 1rem; flex-wrap: wrap; }
.resumen-pago .acciones { display: flex; gap: .5rem; flex-wrap: wrap; }
.btn-prim { background: #003366; color: #ffc107; border: none; padding: .5rem .9rem; border-radius: 10px; font-weight: 800; cursor: pointer; }
.btn-prim:hover { background: #013C8A; color: #fff; }
.btn-sec { background: #16a34a; color: #fff; border: none; padding: .5rem .9rem; border-radius: 10px; font-weight: 800; cursor: pointer; }
.btn-sec:hover { background: #15803d; }
.btn-outline { background: #fff; border: 1px solid #e5e7eb; color: #111827; padding: .5rem .85rem; border-radius: 10px; font-weight: 700; cursor: pointer; }
.btn-outline:hover { background: #f3f4f6; }

.acciones-secundarias { margin-top: .75rem; }
.btn-imprimir {
  background: #fff; border: 1px solid #e5e7eb; color: #111827; padding: .5rem .85rem;
  border-radius: 10px; cursor: pointer; font-weight: 700;
}
.btn-imprimir:hover { background: #f9fafb; }

/* ===================== Responsive ===================== */
@media (max-width: 900px) {
  .cuotas-pendientes { padding: 1.25rem 1rem 5rem; }
}

@media (max-width: 640px) {
  .page-center { align-items: stretch; }
  .cuotas-pendientes { padding: 1rem 1rem 5rem; }

  /* Buscador apilado y full width */
  .input-wrap { flex-direction: column; align-items: stretch; }
  .btn-clear, .btn-buscar { width: 100%; }

  .datos-socio .grid { grid-template-columns: 1fr; }
  .seleccionar-th { width: 120px; }

  /* Barra fija: evitar solapado y desbordes */
  :root { --resumen-h: 128px; }
  .cuotas-pendientes { padding-bottom: calc(var(--resumen-h) + 20px); }
  .resumen-pago {
    position: fixed; bottom: 12px; left: 12px; right: 12px; z-index: 100;
    border-radius: 14px; box-shadow: 0 10px 24px rgba(0,0,0,.18);
    overflow: hidden; /* respeta el borde redondeado */
  }
  .resumen-pago .stats { flex: 1 1 auto; min-width: 160px; }
  .resumen-pago .acciones { flex: 1 1 auto; display: grid; grid-template-columns: 1fr; gap: .5rem; }
  .resumen-pago .btn-outline, .resumen-pago .btn-sec, .resumen-pago .btn-prim { width: 100%; }
}

/* Print */
@media print {
  .page-center { padding: 0; background: none; }
  .buscador, .resumen-pago, .acciones-secundarias { display: none !important; }
  .cuotas-pendientes { box-shadow: none; padding: 0; }
}
/* === Fix: resumen apilado, full-width y fijo en pantallas chicas === */
@media (max-width: 560px) {
  .seleccion-resumen {
    flex-direction: column;       /* apila stats + acciones */
    align-items: stretch;         /* estira al ancho */
    gap: 10px;

    position: fixed;              /* ✅ siempre visible */
    left: 12px;
    right: 12px;
    bottom: 12px;
    z-index: 100;

    background: #fff;             /* fondo sólido */
    border-radius: 14px;
    box-shadow: 0 10px 24px rgba(0,0,0,.18);
    padding: 12px;                /* espacio interno */
  }

  .seleccion-resumen .stats {
    display: grid;
    grid-template-columns: 1fr;   /* cada item en su fila */
    gap: 6px;
  }

  .acciones-resumen {
    width: 100%;
    display: grid;                /* botones en columna */
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .acciones-resumen .btn-outline,
  .acciones-resumen .btn-pagar {
    width: 100%;                  /* full width */
  }

  /* ✅ deja espacio al final para que no tape contenido */
  .cobrador-container {
    padding-bottom: 160px; /* ajustá según alto real de la barra */
  }
}

</style>
