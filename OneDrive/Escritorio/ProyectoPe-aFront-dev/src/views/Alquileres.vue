<template>
  <div class="formulario-container">
    <div class="page-header">
      <h1>{{ modoEdicion ? 'Editar Alquiler de Salón' : 'Registrar Alquiler de Salón' }}</h1>
    </div>

    <form @submit.prevent="submitAlquiler" class="row">
      <!-- Datos del Cliente -->
      <div>
        <label for="nombre" class="form-label">Nombre:</label>
        <input id="nombre" v-model="form.nombre" type="text" class="input-custom" required />
      </div>

      <div>
        <label for="dni" class="form-label">DNI:</label>
        <input id="dni" v-model="form.dni" type="number" class="input-custom" required />
      </div>

      <div>
        <label for="telefono" class="form-label">Teléfono:</label>
        <input id="telefono" v-model="form.telefono" type="tel" class="input-custom" required />
      </div>

      <!-- Información del Alquiler -->
      <div>
        <label for="fecha" class="form-label">Fecha y Hora:</label>
        <div class="input-custom" style="background-color: #e9ecef; cursor: not-allowed;">
          {{ new Date(form.fecha).toLocaleString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' }) }}
        </div>
        <input type="hidden" id="fecha" v-model="form.fecha" />
      </div>

      <div>
        <label for="observaciones" class="form-label">Observaciones:</label>
        <textarea id="observaciones" v-model="form.observaciones" class="input-custom" rows="2" />
      </div>

      <div>
        <label for="condicion" class="form-label">Condición:</label>
        <select id="condicion" v-model="form.condicion" class="input-custom">
          <option :value="true">Activo</option>
          <option :value="false">Inactivo</option>
        </select>
      </div>

      <div>
        <label for="metodo" class="form-label">Método de Pago:</label>
        <select id="metodo" v-model.number="form.metodoPagoId" class="input-custom" required>
          <option value="" disabled>Seleccione un método</option>
          <option v-for="metodo in metodosPago" :key="metodo.metodoPagoId" :value="metodo.metodoPagoId">
            {{ metodo.nombre }}
          </option>
        </select>
      </div>

      <div>
        <label for="salon" class="form-label">Salón:</label>
        <select id="salon" v-model="form.salonId" class="input-custom" required>
          <option :value="null" disabled>Seleccione un salón</option>
          <option v-for="salon in salones" :key="salon.salonId" :value="salon.salonId">
            {{ salon.nombre }}
          </option>
        </select>
      </div>

      <div>
        <label for="monto" class="form-label">Monto:</label>
        <input id="monto" v-model.number="form.monto" type="number" step="0.01" class="input-custom" required />
      </div>

      <!-- Acciones -->
      <div class="form-actions" style="flex: 0 0 100%; justify-content: flex-end; gap: 1rem;">
        <button type="submit" class="btn-boca-primary">{{ modoEdicion ? 'Actualizar' : 'Registrar' }}</button>
        <button type="button" @click="cancelar" class="btn-custom-cancel">Cancelar</button>
      </div>
    </form>

    <p v-if="error" class="msg error">⚠️ {{ error }}</p>
    <p v-if="success" class="msg success">✅ {{ success }}</p>
  </div>
</template>


<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const form = ref({
  nombre: '',
  dni: '',
  telefono: '',
  fecha: '',
  observaciones: '',
  monto: null,
  condicion: true,
  metodoPagoId: null,
  salonId: null,
  id: null
})

const modoEdicion = ref(false)
const alquileres = ref([])
const metodosPago = ref([])
const salones = ref([])
const error = ref('')
const success = ref('')

const METODOS_PAGO_API_URL = 'http://localhost:8081/metodopago'

const salonSeleccionado = computed(() =>
  salones.value.find(salon => salon.salonId === Number(form.value.salonId))
)

watch(() => form.value.salonId, (nuevoId) => {
  const salon = salones.value.find(s => s.salonId === Number(nuevoId))
  if (salon) {
    form.value.monto = salon.precio
  }
})

function cancelar() {
  router.push('alquileres/calendario')
}

async function fetchAlquileres() {
  error.value = ''
  try {
    const res = await axios.get('http://localhost:8081/alquileres')
    alquileres.value = res.data
  } catch (e) {
    error.value = e.message || 'Error al cargar la lista de alquileres'
  }
}

const formatoMoneda = (num) => {
  if (isNaN(num)) return '0,00'
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(num)
}


async function fetchMetodosPago() {
  error.value = ''
  try {
    const res = await axios.get(METODOS_PAGO_API_URL)
    metodosPago.value = res.data
  } catch (e) {
    error.value = e.message || 'Error al cargar la lista de métodos de pago'
  }
}

async function fetchSalones() {
  error.value = ''
  try {
    const res = await axios.get('http://localhost:8081/salones')
    salones.value = res.data
  } catch (e) {
    error.value = e.message || 'Error al cargar la lista de salones'
  }
}

async function submitAlquiler() {
  error.value = ''
  success.value = ''

  const salonIdNum = Number(form.value.salonId)
  if (!salonIdNum) {
    error.value = 'Debe seleccionar un salón válido'
    return
  }

  const metodoPagoNum = Number(form.value.metodoPagoId)
  if (!metodoPagoNum) {
    error.value = 'Debe seleccionar un método de pago válido'
    return
  }

  try {
    const d = new Date(form.value.fecha)
    const formattedFecha = d.toISOString().substring(0, 19)

    const payload = {
      nombre: form.value.nombre,
      dni: form.value.dni,
      telefono: form.value.telefono,
      fecha: formattedFecha,
      observaciones: form.value.observaciones,
      monto: form.value.monto,
      condicion: form.value.condicion,
      metodoPagoId: metodoPagoNum,
      salonId: salonIdNum
    }

    if (modoEdicion.value && form.value.id) {
      await axios.put(`http://localhost:8081/alquileres/${form.value.id}`, payload)
      success.value = 'Alquiler actualizado exitosamente'
    } else {
      await axios.post('http://localhost:8081/alquileres', payload)
      success.value = 'Alquiler registrado exitosamente'
    }

    // Limpiar y redirigir
    form.value = {
      salonId: null,
      nombre: '',
      dni: '',
      telefono: '',
      fecha: '',
      observaciones: '',
      monto: null,
      condicion: true,
      metodoPagoId: null,
      id: null
    }
    modoEdicion.value = false
    fetchAlquileres()

    setTimeout(() => router.push('alquileres/calendario'), 500)
  } catch (e) {
    error.value = e.response?.data?.error || e.message || 'Error al registrar alquiler'
  }
}

onMounted(() => {
  fetchAlquileres()
  fetchMetodosPago()
  fetchSalones()

  const fechaParam = route.query.fecha
  const editarParam = route.query.editar
  const datosParam = route.query.datos

  if (editarParam === 'true' && datosParam) {
    try {
      const data = JSON.parse(decodeURIComponent(datosParam))
      form.value = {
        nombre: data.nombre || '',
        dni: data.dni || '',
        telefono: data.telefono || '',
        fecha: data.fecha?.slice(0, 16) || '',
        observaciones: data.observaciones || '',
        monto: data.monto || null,
        condicion: data.condicion ?? true,
        metodoPagoId: data.metodoPagoId || null,
        salonId: data.salonId || null,
        id: data.id || null
      }
      modoEdicion.value = true
    } catch (e) {
      console.error('Error al parsear datos de edición:', e)
    }
  } else if (fechaParam) {
    form.value.fecha = fechaParam
  }
})
</script>

<style scoped>
/* --- Título --- */
.page-header h1 {
  font-size: 2.5rem;
  font-weight: 900;
  color: #003366;
  text-align: center;
  margin-bottom: 0.2rem;
  letter-spacing: 0.04em;
  user-select: none;
  position: relative;
  margin-bottom: 2rem;
}

.page-header h1::after {
  content: "";
  display: block;
  width: 120px;
  height: 4px;
  background: #ffc107;
  margin: 0.3rem auto 0;
  border-radius: 2px;
}

/* --- Formulario --- */
.formulario-container {
  margin-top: 2.8rem;
  max-width: 950px;       /* ancho máximo */
  margin-left: auto;      /* centrado horizontal */
  margin-right: auto;     /* centrado horizontal */
  padding: 4rem 2.5rem;
  background: #f8f9fa;
  border-radius: 0.75rem;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;    /* CENTRA contenido horizontalmente */
}

/* Para que el formulario ocupe todo el ancho disponible dentro del contenedor */
form.row {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  row-gap: 1.5rem;
  column-gap: 1.5rem;
}

/* ... el resto igual ... */


form.row {
  display: flex;
  flex-wrap: wrap;
  row-gap: 1.5rem;
  column-gap: 1.5rem;
}

form.row > div {
  flex: 0 0 100%;
  max-width: 100%;
}

@media (min-width: 768px) {
  form.row > div {
    flex: 0 0 48%;
    max-width: 48%;
  }
}

@media (min-width: 1200px) {
  form.row > div {
    flex: 0 0 30%;
    max-width: 30%;
  }
}


.form-label {
  display: block;
  font-weight: 600;
  color: #003366;
  margin-bottom: 4px;
  user-select: none;
}

.input-custom {
  width: 100%;
  padding: 10px 12px;
  border: 1.5px solid #cbd5e1;
  border-radius: 0.375rem;
  font-size: 1rem;
  transition: border 0.3s ease, box-shadow 0.3s ease;
}

.input-custom:focus {
  border-color: #00509e;
  outline: none;
  box-shadow: 0 0 6px rgba(0, 80, 158, 0.4);
}

/* --- Botones --- */
.btn-boca-primary {
  background-color: #003366;
  color: #ffc107;
  border: none;
  border-radius: 0.5rem;
  padding: 0.55rem 1.25rem;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease, color 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-boca-primary:hover,
.btn-boca-primary:focus {
  background-color: #00509e;
  color: #ffffff;
  outline: none;
}

.btn-custom-cancel {
  background-color: transparent;
  color: #003366;
  border: 1.5px solid #003366;
  border-radius: 0.5rem;
  padding: 0.5rem 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-custom-cancel:hover {
  background-color: #f2f6fc;
  color: #00509e;
  border-color: #00509e;
}

/* --- Acciones del Formulario --- */
.form-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

/* --- Mensajes de estado --- */
.msg {
  font-weight: bold;
  font-size: 1rem;
  margin-top: 1rem;
  text-align: center;
}

.msg.error {
  color: #dc3545;
}

.msg.success {
  color: #198754;
}
</style>