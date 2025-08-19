<!-- <template>
  <div class="container mt-5">
    <h3>Registrar Pago</h3>

    <form @submit.prevent="registrarPago">
      <div class="mb-3">
        <label for="viajeId" class="form-label">Viaje</label>
        <select v-model="pago.viajeId" class="form-select" required>
          <option disabled value="">Seleccione un viaje</option>
          <option v-for="viaje in viajes" :key="viaje.id" :value="viaje.viajeBomboneraId">
            {{ viaje.destino }}
          </option>
        </select>
      </div>
      <div class="mb-3">
        <label>DNI</label>
        <input type="text" v-model="pago.dni" class="form-control" required />
      </div>

      <div class="mb-3">
        <label>Nombre</label>
        <input type="text" v-model="pago.nombre" class="form-control" required />
      </div>

      <div class="mb-3">
        <label>Apellido</label>
        <input type="text" v-model="pago.apellido" class="form-control" required />
      </div>

      <div class="mb-3">
        <label>Monto</label>
        <input type="number" step="0.01" v-model="pago.monto" class="form-control" required />
      </div>

      <div class="mb-3">
        <label for="metodoPagoId" class="form-label">Método de Pago</label>
        <select v-model="pago.metodoPagoId" class="form-select" required>
          <option disabled value="">Seleccione un método</option>
          <option v-for="metodo in metodosPago" :key="metodo.id" :value="metodo.metodoPagoId">
            {{ metodo.nombre }}
          </option>
        </select>
      </div>

      <button class="btn btn-primary" type="submit">Registrar Pago</button>
    </form>
  </div>
</template>
<script>
</script> -->

<!-- <script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'

const pago = reactive({
  viajeId: '',
  monto: null,
  nombre: '',
  apellido: '',
  dni: '',
  metodoPagoId: ''
})

const viajes = ref([])
const metodosPago = ref([])

onMounted(() => {
  cargarViajes()
  cargarMetodosPago()
})

async function cargarViajes() {
  try {
    const response = await axios.get('http://127.0.0.1:8081/viajeBombonera')
    viajes.value = response.data
  } catch (error) {
    alert('Error al cargar viajes')
    console.error(error)
  }
}

async function cargarMetodosPago() {
  try {
    const response = await axios.get('http://127.0.0.1:8081/metodopago')
    metodosPago.value = response.data
  } catch (error) {
    alert('Error al cargar métodos de pago')
    console.error(error)
  }
}

async function registrarPago() {
  try {
    const payload = { ...pago }

    // Validación básica
    if (!payload.nombre || !payload.apellido || !payload.dni) {
      alert('Debe completar nombre, apellido y DNI')
      return
    }
    console.log('Payload enviado:', payload)

    await axios.post('http://127.0.0.1:8081/viajesPagos', payload)
    alert('✅ Pago registrado con éxito')
    
    // Limpiar formulario
    pago.nombre = ''
    pago.apellido = ''
    pago.dni = ''
    pago.monto = null
    pago.viajeId = ''
    pago.metodoPagoId = ''
  } catch (error) {
    alert(`❌ Error al registrar pago: ${error.response?.data || error.message}`)
    console.error(error)
  }
}
</script> -->
