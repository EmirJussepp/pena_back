<template>
  <div class="container mt-5">
    <div class="formulario-container">
      <h3 class="form-title">Registrar Cobrador</h3>

      <form @submit.prevent="guardarCobrador" class="row g-3">
        <div class="col-md-6">
          <label class="form-label" for="nombre">Nombre</label>
          <input id="nombre" v-model="form.nombre" class="form-control" required />
        </div>
        <div class="col-md-6">
          <label class="form-label" for="telefono">Teléfono</label>
          <input id="telefono" v-model="form.telefono" class="form-control" required />
        </div>
        <div class="col-md-6">
          <label class="form-label" for="dni">DNI</label>
          <input id="dni" v-model="form.dni" class="form-control" required />
        </div>
        <div class="col-md-6">
          <label class="form-label" for="zona">Zona</label>
          <input id="zona" v-model="form.zona" class="form-control" required />
        </div>

        <div class="col-12 d-flex justify-content-end gap-2 mt-3">
          <button type="submit" class="btn-boca-primary">
            {{ editandoId ? 'Actualizar' : 'Crear' }}
          </button>
          <button v-if="editandoId" @click="cancelarEdicion" class="btn-custom-cancel" type="button">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Tabla de cobradores -->
    <div class="card shadow-sm p-4 mt-4">
      <h3 class="form-title">Cobradores Registrados</h3>
      <div class="table-responsive mt-3">
        <table class="professional-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Teléfono</th>
              <th>DNI</th>
              <th>Zona</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cobrador in cobradores" :key="cobrador.cobradoresId">
              <td>{{ cobrador.nombre }}</td>
              <td>{{ cobrador.telefono }}</td>
              <td>{{ cobrador.dni }}</td>
              <td>{{ cobrador.zona }}</td>
              <td>
              <button
                @click="editarCobrador(cobrador)"
                class="btn btn-sm btn-outline-primary me-2"
                title="Editar"
              >
               <i class="bi bi-pencil-square"></i>
                </button>
                <button
                  @click="eliminarCobrador(cobrador.cobradoresId)"
                  class="btn btn-sm btn-outline-danger"
                  title="Eliminar"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
            <tr v-if="cobradores.length === 0">
              <td colspan="5" class="text-center text-muted py-3">No hay cobradores registrados.</td>
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
import Swal from 'sweetalert2'

const cobradores = ref([])
const editandoId = ref(null)
const form = ref({
  nombre: '',
  telefono: '',
  dni: '',
  zona: ''
})

async function obtenerCobradores() {
  try {
    const res = await axios.get('http://localhost:8081/cobradores')
    cobradores.value = res.data
  } catch (error) {
    Swal.fire('Error', 'Error al obtener cobradores', 'error')
    console.error(error)
  }
}

function editarCobrador(cobrador) {
  editandoId.value = cobrador.cobradoresId
  form.value = { ...cobrador }
}

function cancelarEdicion() {
  editandoId.value = null
  form.value = { nombre: '', telefono: '', dni: '', zona: '' }
}

async function guardarCobrador() {
  try {
    if (editandoId.value) {
      await axios.patch(`http://localhost:8081/cobradores/${editandoId.value}`, form.value)
      Swal.fire('Actualizado', 'Cobrador actualizado con éxito', 'success')
    } else {
      await axios.post('http://localhost:8081/cobradores', form.value)
      Swal.fire('Creado', 'Cobrador creado con éxito', 'success')
    }
    cancelarEdicion()
    await obtenerCobradores()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al guardar cobrador'
    Swal.fire('Error', msg, 'error')
    console.error(error)
  }
}

async function eliminarCobrador(id) {
  const confirm = await Swal.fire({
    title: '¿Eliminar cobrador?',
    text: 'Esta acción no se puede deshacer',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar'
  })

  if (!confirm.isConfirmed) return

  try {
    await axios.delete(`http://localhost:8081/cobradores/${id}`)
    Swal.fire('Eliminado', 'Cobrador eliminado con éxito', 'success')
    await obtenerCobradores()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al eliminar cobrador'
    Swal.fire('Error', msg, 'error')
    console.error(error)
  }
}

onMounted(obtenerCobradores)
</script>


<style scoped>
/* Copia el mismo CSS que tenés para que quede igual */
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
