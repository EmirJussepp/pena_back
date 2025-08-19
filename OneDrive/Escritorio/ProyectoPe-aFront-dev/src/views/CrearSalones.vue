<template>
  <div class="container mt-5">
    <!-- Card para registrar o actualizar un salón -->
   <div class="formulario-container">
    <h3 class="form-title">{{ editandoId ? 'Editar Salón' : 'Registro de Salones' }}</h3>

      <form @submit.prevent="guardarSalon" style="margin-top: 1.5rem;">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label fw-semibold" for="nombre">Nombre</label>
            <input
              id="nombre"
              v-model="nuevoSalon.nombre"
              type="text"
              class="form-control"
              required
              placeholder="Ingrese nombre del salón"
            />
          </div>

          <div class="col-md-6">
            <label class="form-label fw-semibold" for="precio">Precio</label>
            <input
              id="precio"
              v-model.number="nuevoSalon.precio"
              type="number"
              step="0.01"
              class="form-control"
              required
              placeholder="Ingrese precio"
              min="0"
            />
          </div>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <button type="submit" class="btn-boca-primary">
            {{ editandoId ? 'Actualizar Salón' : 'Crear Salón' }}
          </button>
          <button v-if="editandoId" type="button" @click="cancelarEdicion" class="btn-custom-cancel">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Card para mostrar lista de salones -->
    <div class="card shadow-sm p-4 mt-4">
      <h3 class="form-title">
        Lista de Salones
      </h3>

      <div class="table-responsive" style="margin-top: 1rem;">
        <table class="professional-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Precio</th>
              <th class="text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="salon in salones" :key="salon.salonId">
              <td>{{ salon.nombre }}</td>
              <td>${{ formatoMoneda(salon.precio) }}</td>
              <td class="text-center">
                <button @click="editarSalon(salon)" class="btn btn-sm btn-outline-primary me-2" title="Editar">
                  <i class="bi bi-pencil-fill"></i>
                </button>
                <button @click="eliminarSalon(salon.salonId)" class="btn btn-sm btn-outline-danger" title="Eliminar">
                  <i class="bi bi-trash-fill"></i>
                </button>
              </td>
            </tr>
            <tr v-if="salones.length === 0">
              <td colspan="3" class="text-center text-muted py-3">No hay salones registrados.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const salones = ref([])
const nuevoSalon = ref({
  nombre: '',
  precio: 0,
})
const editandoId = ref(null)

async function obtenerSalones() {
  try {
    const res = await axios.get('http://localhost:8081/salones')
    salones.value = res.data
  } catch (error) {
    alert('Error al obtener salones')
    console.error(error)
  }
}

const formatoMoneda = (num) => {
  if (isNaN(num)) return '0,00'
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(num)
}

function editarSalon(salon) {
  editandoId.value = salon.salonId
  nuevoSalon.value = { nombre: salon.nombre, precio: salon.precio }
}

function cancelarEdicion() {
  editandoId.value = null
  nuevoSalon.value = { nombre: '', precio: 0 }
}

async function guardarSalon() {
  try {
    if (editandoId.value) {
      // Actualizar
      await axios.patch(`http://localhost:8081/salones/${editandoId.value}`, nuevoSalon.value)
      alert('Salón actualizado exitosamente')
    } else {
      // Crear
      await axios.post('http://localhost:8081/salones', nuevoSalon.value)
      alert('Salón creado exitosamente')
    }
    cancelarEdicion()
    await obtenerSalones()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al guardar salón'
    alert(msg)
    console.error(error)
  }
}

async function eliminarSalon(id) {
  if (!confirm('¿Estás seguro que deseas eliminar este salón?')) return
  try {
    await axios.delete(`http://localhost:8081/salones/${id}`)
    alert('Salón eliminado exitosamente')
    await obtenerSalones()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al eliminar salón'
    alert(msg)
    console.error(error)
  }
}

onMounted(obtenerSalones)
</script>


<style scoped>
.formulario-container {
  max-width: 950px;
  margin-left: auto;
  margin-right: auto;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgb(0 51 102 / 0.15);
  user-select: none;
  padding: 2rem 2.5rem;
}

.form-title {
  font-weight: 900;
  font-size: 2rem;
  color: #003366;
  text-align: center;
  margin-bottom: 1rem;
  position: relative;
  user-select: none;
}

.form-title::after {
  content: "";
  display: block;
  width: 120px;
  height: 3.5px;
  background: #ffc107;
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

.professional-table {
  border-collapse: separate !important;
  border-spacing: 0 8px;
  width: 100%;
  font-size: 0.9rem;
  color: #0b2240;
  box-shadow: 0 2px 6px rgb(0 0 0 / 0.1);
  background-color: #ffffff;
  border-radius: 0.5rem;
  overflow: hidden;
}

.professional-table thead tr {
  background-color: #003366;
  color: #ffffff;
}

.professional-table thead th {
  padding: 12px 15px;
  text-align: left;
  font-weight: 600;
  border-bottom: none;
  user-select: none;
}

.professional-table tbody tr {
  background: #f0f6fc;
  border-radius: 0.375rem;
  box-shadow: 0 0 6px rgb(0 0 0 / 0.05);
  transition: box-shadow 0.2s ease;
  cursor: default;
}

.professional-table tbody tr:hover {
  box-shadow: 0 0 10px rgb(0 0 0 / 0.15);
  background-color: #dcefff;
}

.professional-table tbody td {
  padding: 10px 15px;
  vertical-align: middle;
  border: none;
  white-space: nowrap;
}

.professional-table tbody tr td:last-child {
  text-align: center;
}

input.form-control {
  border-radius: 0.375rem;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.1);
  transition: box-shadow 0.3s ease;
  padding: 0.5rem 0.75rem;
}

input.form-control:focus {
  box-shadow: 0 0 8px #00509e;
  border-color: #00509e;
  outline: none;
}

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
  color: #fff;
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

.container.mt-5 {
  max-width: 950px;
  margin-left: auto;
  margin-right: auto;
}

.card + .card {
  margin-top: 2rem;
}
</style>
