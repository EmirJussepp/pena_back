<template>
  <div class="formulario-container">
    <h2 class="form-title">Calendario</h2>

    <div style="display: flex; gap: 24px;">
      <!-- Calendario -->
      <div style="flex: 1;">
        <VueCal
          :events="eventos"
          style="height: 600px; border-radius: 0.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08);"
          view="month"
          time="none"
          :disable-views="['day', 'years', 'week', 'year']"
          locale="es"
          @cell-click="irARegistro"
        />
      </div>

      <!-- Panel lateral -->
      <div v-if="mostrarPanel" class="panel-lateral">
  <div style="position: relative; margin-bottom: 0.5rem;">
    <h5 class="panel-titulo">{{ fechaSeleccionada }}</h5>
    <button
      @click="cerrarPanel"
      class="btn-cerrar-panel"
      title="Cerrar panel"
      style="position: absolute; top: 0; right: 0;"
    >
      <i class="fas fa-times"></i>
    </button>
</div>

 
        <div v-if="alquileresDelDia.length === 0" class="panel-vacio">
          No hay alquileres para este día.
        </div>

        <div v-else>
          <div
            v-for="(alquiler, index) in alquileresDelDia"
            :key="alquiler.alquilerId"
            @click="seleccionarAlquiler(index)"
            :class="['card-alquiler', { seleccionado: alquilerSeleccionadoIndex === index }]"
          >
            <strong>{{ alquiler.nombre }}</strong><br />
            Salón: {{ salonNombre(alquiler.salonId) }}<br />
            Monto: ${{ alquiler.monto }}
          </div>

          <div v-if="alquilerSeleccionado"  class="panel-detalle">
        <h5 class="detalle-titulo">Detalle del Alquiler</h5>

            <template v-if="!editando">
              <p><b>Nombre:</b> {{ alquilerSeleccionado.nombre }}</p>
              <p><b>Salón:</b> {{ salonNombre(alquilerSeleccionado.salonId) }}</p>
              <p><b>Fecha:</b> {{ new Date(alquilerSeleccionado.fecha).toLocaleString() }}</p>
              <p><b>Teléfono:</b> {{ alquilerSeleccionado.telefono || '-' }}</p>
              <p><b>Monto:</b> ${{ alquilerSeleccionado.monto }}</p>
              <p><b>Observaciones:</b> {{ alquilerSeleccionado.observaciones || '-' }}</p>
              <p><b>Condición:</b> {{ alquilerSeleccionado.condicion ? 'Sí' : 'No' }}</p>
              <p><b>Método de Pago:</b> {{ metodoPagoNombre(alquilerSeleccionado.metodoPagoId) }}</p>
              <p><b>DNI:</b> {{ alquilerSeleccionado.dni }}</p>

              <div class="botones-panel">
                <button @click="startEditar" class="btn-boca-primary">Editar</button>
                <button @click="confirmarEliminar(alquilerSeleccionado.alquilerId)" class="btn-custom-cancel">Eliminar</button>
              </div>
            </template>

            <template v-else>
              <div class="form-group">
                <label for="nombre">Nombre</label>
                <input id="nombre" v-model="form.nombre" placeholder="Nombre" class="form-control" />
              </div>

              <div class="form-group">
                <label for="telefono">Teléfono</label>
                <input id="telefono" v-model="form.telefono" placeholder="Teléfono" class="form-control" />
              </div>

              <div class="form-group">
                <label for="monto">Monto</label>
                <input id="monto" v-model.number="form.monto" type="number" placeholder="Monto" class="form-control" />
              </div>

              <div class="form-group">
                <label for="salonId">Salón</label>
                <select id="salonId" v-model.number="form.salonId" class="form-control">
                  <option disabled value="0">Seleccione un salón</option>
                  <option v-for="salon in salones" :key="salon.salonId" :value="salon.salonId">{{ salon.nombre }}</option>
                </select>
              </div>

              <div class="form-group">
                <label for="observaciones">Observaciones</label>
                <input id="observaciones" v-model="form.observaciones" placeholder="Observaciones" class="form-control" />
              </div>

              <div class="form-group">
                <label for="condicion">Condición</label>
                <select id="condicion" v-model="form.condicion" class="form-control">
                  <option :value="true">Sí</option>
                  <option :value="false">No</option>
                </select>
              </div>

              <div class="form-group">
                <label for="metodoPagoId">Método de Pago</label>
                <select id="metodoPagoId" v-model.number="form.metodoPagoId" class="form-control">
                  <option disabled value="0">Seleccione un método de pago</option>
                  <option v-for="metodo in metodosPago" :key="metodo.metodoPagoId" :value="metodo.metodoPagoId">{{ metodo.nombre }}</option>
                </select>
              </div>

              <div class="form-group">
                <label for="dni">DNI</label>
                <input id="dni" v-model="form.dni" placeholder="DNI" class="form-control" />
              </div>

              <div class="form-group">
                <label for="fecha">Fecha y hora</label>
                <input id="fecha" v-model="form.fecha" type="datetime-local" placeholder="Fecha y hora" class="form-control" />
              </div>

              <div class="botones-panel">
                <button @click="actualizar" class="btn-boca-primary">Guardar</button>
                <button @click="cancelar" class="btn-custom-cancel">Cancelar</button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'
import axios from 'axios'
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'

const alquileres = ref([])
const eventos = ref([])
const salones = ref([])
const metodosPago = ref([])
const router = useRouter()

const mostrarPanel = ref(false)
const alquileresDelDia = ref([])
const alquilerSeleccionadoIndex = ref(null)
const fechaSeleccionada = ref('')
const editando = ref(false)

const form = reactive({
  nombre: '',
  telefono: '',
  monto: 0,
  salonId: 0,
  observaciones: '',
  condicion: true,
  metodoPagoId: 0,
  dni: '',
  fecha: ''
})

function normalizeDateToISO(dateStr) {
  return dateStr.split('T')[0]
}

function cerrarPanel() {
  mostrarPanel.value = false
  alquilerSeleccionadoIndex.value = null
}

function mapAlquileresToEventos() {
  eventos.value = alquileres.value.map(a => {
    const fechaISO = normalizeDateToISO(a.fecha)
    const [year, month, day] = fechaISO.split('-').map(Number)
    const start = new Date(year, month - 1, day, 12, 0, 0)
    const end = new Date(start)
    end.setHours(23, 59, 59, 999)

    return {
      start,
      end,
      title: `${a.nombre} - Salón ${a.salonId}`,
      allDay: true,
      class: 'evento-alquiler',
      backgroundColor: 'transparent',
      color: 'white',
      data: a
    }
  })
}

watch(alquileres, () => mapAlquileresToEventos(), { immediate: true })

async function fetchAlquileres() {
  try {
    const res = await axios.get('http://localhost:8081/alquileres')
    alquileres.value = res.data
  } catch (error) {
    console.error('Error al cargar alquileres:', error)
  }
}

async function obtenerSalones() {
  try {
    const res = await axios.get('http://localhost:8081/salones')
    salones.value = res.data
  } catch (error) {
    alert('Error al obtener salones')
    console.error(error)
  }
}

async function obtenerMetodosPago() {
  try {
    const res = await axios.get('http://localhost:8081/metodopago')
    metodosPago.value = res.data
  } catch (e) {
    console.error('Error al cargar métodos de pago:', e)
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'No se pudieron cargar los métodos de pago.',
      confirmButtonColor: '#003366',
    })
  }
}

function metodoPagoNombre(id) {
  const metodo = metodosPago.value.find(m => m.metodoPagoId === id)
  return metodo ? metodo.nombre : 'Desconocido'
}
function salonNombre(id) {
  const salon = salones.value.find(m => m.salonId === id)
  return salon ? salon.nombre : 'Desconocido'
}

function seleccionarAlquiler(index) {
  alquilerSeleccionadoIndex.value = index
  editando.value = false
  cargarFormulario()
}

const alquilerSeleccionado = computed(() => {
  if (alquilerSeleccionadoIndex.value === null) return null
  return alquileresDelDia.value[alquilerSeleccionadoIndex.value]
})

async function irARegistro(payload) {
  const rawDate = payload?.startDate || payload?.date || payload
  const fecha = new Date(rawDate)
  if (isNaN(fecha.getTime())) return
  fecha.setHours(12, 0, 0, 0)
  const fechaISO = fecha.toISOString().slice(0, 10)
  fechaSeleccionada.value = fechaISO

  const alquileresEnElDia = alquileres.value.filter(a => normalizeDateToISO(a.fecha) === fechaISO)
  alquileresDelDia.value = alquileresEnElDia
  alquilerSeleccionadoIndex.value = alquileresEnElDia.length > 0 ? 0 : null
  mostrarPanel.value = alquileresEnElDia.length > 0

  if (alquileresEnElDia.length === 0) {
    mostrarPanel.value = false
    router.push({ path: '/alquiler', query: { fecha: fecha.toISOString().slice(0, 16) } })
  }
}

function cargarFormulario() {
  if (!alquilerSeleccionado.value) return
  const a = alquilerSeleccionado.value
  form.nombre = a.nombre || ''
  form.telefono = a.telefono || ''
  form.monto = Number(a.monto) || 0
  form.salonId = a.salonId || 0
  form.observaciones = a.observaciones || ''
  form.condicion = a.condicion ?? true
  form.metodoPagoId = a.metodoPagoId || 0
  form.dni = a.dni || ''
  form.fecha = formatDateTimeLocal(a.fecha)
}

async function confirmarEliminar(id) {
  const result = await Swal.fire({
    title: '¿Eliminar?',
    text: 'Esta acción no se puede deshacer.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  })

  if (result.isConfirmed) await eliminarAlquiler(id)
}

async function eliminarAlquiler(id) {
  try {
    await axios.delete(`http://localhost:8081/alquileres/${id}`)
    await fetchAlquileres()
    Swal.fire('Eliminado', 'El alquiler fue eliminado con éxito', 'success')
    // mostrarPanel.value = false
       location.reload()  // recarga la página completa
  } catch (error) {
    Swal.fire('Error', 'No se pudo eliminar el alquiler', 'error')
  }
}

async function actualizarAlquiler(data) {
  try {
    await axios.patch(`http://localhost:8081/alquileres/${data.alquilerId}`, data)
    await fetchAlquileres()
    Swal.fire('Actualizado', 'El alquiler fue actualizado con éxito', 'success')
    location.reload()
  } catch (error) {
    const mensaje = error?.response?.data?.mensaje || 'No se pudo actualizar el alquiler'
    Swal.fire('Error', mensaje, 'error')
  }
}


function startEditar() {
  editando.value = true
  cargarFormulario()
}

// Esta función convierte el valor del <input type="datetime-local">
// (ej: "2025-07-16T15:30") a formato ISO local sin zona horaria,
// con segundos a "00". Ejemplo: "2025-07-16T15:30:00"
function toLocalDateTimeString(datetimeLocal) {
  if (!datetimeLocal) return ''
  if (datetimeLocal.length === 16) {
    return datetimeLocal + ':00'
  }
  return datetimeLocal
}

async function actualizar() {
  if (!form.nombre || !form.fecha) {
    await Swal.fire('Error', 'Nombre y fecha son obligatorios', 'error')
    return
  }
  const actualizado = {
    ...alquilerSeleccionado.value,
    nombre: form.nombre,
    telefono: form.telefono,
    monto: form.monto.toString(),
    salonId: form.salonId || 0,
    metodoPagoId: form.metodoPagoId || 0,
    observaciones: form.observaciones,
    condicion: Boolean(form.condicion),
    dni: form.dni,
    fecha: toLocalDateTimeString(form.fecha)
  }
  await actualizarAlquiler(actualizado)
  editando.value = false
}

function cancelar() {
  editando.value = false
}

// Esta función toma una fecha ISO (con zona horaria o sin ella) y
// la formatea para usarla en el input datetime-local (que requiere "YYYY-MM-DDTHH:mm")
function formatDateTimeLocal(fechaISO) {
  if (!fechaISO) return ''
  const d = new Date(fechaISO)
  if (isNaN(d.getTime())) return ''
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  fetchAlquileres()
  obtenerSalones()
  obtenerMetodosPago()
})
</script>

<style scoped>
h2 {
  font-size: 2.2rem;
  font-weight: 900;
  color: #003366;
  text-align: center;
  margin-bottom: 0.5rem;
  letter-spacing: 0.04em;
  user-select: none;
  position: relative;
  max-width: 300px;
  margin-left: auto;
  margin-right: auto;
}

h2::after {
  content: "";
  display: block;
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, #ffc107, #ffb300);
  margin: 0.3rem auto 0;
  border-radius: 2px;
  box-shadow: 0 0 6px #ffb300aa;
}
/* ⚠️ VueCal - Deep Styles */
::v-deep(.vuecal) {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  --vuecal-primary: #003366;
  --vuecal-text: #003366;
  --vuecal-accent: #ffc107;
  --vuecal-bg: #f4f8fc;
  --vuecal-border: #003366;
  --vuecal-dark: #002244;
  --vuecal-hover: #e8f0fe;
}

::v-deep(.vuecal__event.evento-alquiler) {
  background-color: #003366 !important;
  color: #ffc107 !important;
  padding: 5px 8px;
  border-radius: 10px;
  font-weight: bold;
  font-size: 0.9rem;
  border: 1px solid #ffc107;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

::v-deep(.vuecal__cell--today) {
  background-color: #ffe58f !important;
  border-radius: 8px;
  box-shadow: inset 0 0 6px #d0a500;
}
::v-deep(.vuecal__cell--has-events) {
  background-color: #003366 !important;
  border-radius: 10px;
  color: #ffc107 !important;
}

::v-deep(.vuecal__cell--selected) {
  box-shadow: 0 0 0 2px #ffc107 inset !important;
  background-color: #00509e !important;
  color: white;
}

::v-deep(.vuecal__title-bar),
::v-deep(.vuecal__arrow),
::v-deep(.vuecal__title) {
  color: #003366;
  font-weight: bold;
  font-size: 1rem;
}

::v-deep(.vuecal__weekdays) {
  background-color: #003366;
  color: #ffc107;
  font-weight: 600;
  border-radius: 6px 6px 0 0;
}

::v-deep(.vuecal__cell) {
  transition: background-color 0.2s ease;
  cursor: pointer;
}/* Panel lateral */
.panel-lateral {
  width: 360px;
  background: #f5f8fb;
  border-radius: 8px;
  box-shadow: 0 3px 12px rgb(0 0 0 / 0.1);
  padding: 1.5rem 1rem 1.5rem 1rem;
  overflow-y: auto;
  max-height: 600px;
  user-select: none;
}

.panel-titulo,
.detalle-titulo {
  font-weight: 700;
  font-size: 1.3rem;
  color: #003366;
  text-align: center;
  user-select: none;
  position: relative;
  max-width: 260px;
  margin-left: auto;
  margin-right: auto;
}

.panel-titulo::after,
.detalle-titulo::after {
  content: "";
  display: block;
  width: 90px;
  height: 3px;
  background: linear-gradient(90deg, #ffc107, #ffb300);
  margin: 0.3rem auto 0;
  border-radius: 2px;
  box-shadow: 0 0 5px #ffb300aa;
}


.panel-vacio {
  font-style: italic;
  color: #777;
  text-align: center;
  margin: 1.5rem 0;
}

.card-alquiler {
  padding: 0.65rem 1rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 0 10px rgb(0 0 0 / 0.05);
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: 0.2s ease;
  user-select: none;
}

.card-alquiler:hover {
  box-shadow: 0 0 12px rgb(0 0 0 / 0.12);
}

.card-alquiler.seleccionado {
  border: 2px solid #003366;
  background: #eaf3fb;
  box-shadow: none;
}


/* Botones */
.btn-boca-primary {
  background-color: #003366;
  color: #ffc107;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.3rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
  user-select: none;
  margin-right: 0.8rem;
}

.btn-boca-primary:hover {
  background-color: #002244;
}

.btn-custom-cancel {
  background-color: #ddd;
  color: #333;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.3rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
  user-select: none;
}

.btn-custom-cancel:hover {
  background-color: #bbb;
}

.btn-cerrar-panel {
  background: transparent;
  border: none;
  font-size: 1.3rem;
  color: #333;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.btn-cerrar-panel:hover {
  background-color: #f0f0f0;
  color: #d84315;
  transform: scale(1.05);
}



/* Form group y labels */
.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #003366;
  margin-bottom: 0.3rem;
  user-select: none;
}

/* Inputs */
.form-control {
  width: 100%;
  padding: 0.45rem 0.65rem;
  font-size: 1rem;
  border: 1.5px solid #003366;
  border-radius: 6px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: #ffc107;
  box-shadow: 0 0 6px #ffc107aa;
}

/* Para mantener buen espacio entre título y botón */
.panel-lateral > div:first-child {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}


</style>
