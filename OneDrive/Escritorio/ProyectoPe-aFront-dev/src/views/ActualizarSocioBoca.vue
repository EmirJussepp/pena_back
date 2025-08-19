<template>
  <div class="container mt-5">
    <!-- Formulario de creación / edición -->
    <div class="formulario-container">
      <h3 class="form-title">Registrar Tipo de Socio Boca</h3>

      <form @submit.prevent="crearOActualizarTipo" class="row g-3" style="margin-top: 1.5rem;">
        <div class="col-md-6">
          <label class="form-label" for="nombre">Nombre</label>
          <input
            id="nombre"
            v-model="nuevoTipo.nombre"
            type="text"
            class="form-control"
            required
            placeholder="Ingrese nombre del tipo"
          />
        </div>

        <div class="form-actions col-12 d-flex justify-content-end gap-2">
          <button type="submit" class="btn-boca-primary">
            {{ tipoEditandoId ? 'Actualizar Tipo' : 'Crear Tipo' }}
          </button>
          <button
            v-if="tipoEditandoId"
            type="button"
            @click="cancelarEdicion"
            class="btn-custom-cancel"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Tabla con tipos existentes -->
    <div class="card shadow-sm p-4 mt-4">
      <h3 class="form-title">Tipos de Socio Boca</h3>

      <div class="table-responsive mt-3">
        <table class="professional-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tipo in tipos" :key="tipo.tipoSocioBocaId">
              <td>{{ tipo.nombre }}</td>
              <td>
                <button
                  @click="iniciarEdicion(tipo)"
                  class="btn btn-sm btn-outline-primary me-2"
                  title="Editar"
                >
                  <i class="bi bi-pencil-square"></i>
                </button>
                <button
                  @click="eliminarTipo(tipo.tipoSocioBocaId)"
                  class="btn btn-sm btn-outline-danger"
                  title="Eliminar"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
            <tr v-if="tipos.length === 0">
              <td colspan="2" class="text-center text-muted py-3">
                No hay tipos registrados.
              </td>
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

const tipos = ref([])
const nuevoTipo = ref({ nombre: '' })
const tipoEditandoId = ref(null)

async function obtenerTipos() {
  try {
    const res = await axios.get('http://localhost:8081/sociosboca')
    tipos.value = res.data
  } catch (error) {
    Swal.fire('Error', 'Error al obtener tipos de socio Boca', 'error')
    console.error(error)
  }
}

function iniciarEdicion(tipo) {
  tipoEditandoId.value = tipo.tipoSocioBocaId
  nuevoTipo.value = { nombre: tipo.nombre }
}

function cancelarEdicion() {
  tipoEditandoId.value = null
  nuevoTipo.value = { nombre: '' }
}

async function crearOActualizarTipo() {
  if (tipoEditandoId.value) {
    await actualizarTipo()
  } else {
    await crearTipo()
  }
}

async function crearTipo() {
  try {
    await axios.post('http://localhost:8081/sociosboca', nuevoTipo.value)
    Swal.fire('Creado', 'Tipo de socio Boca creado exitosamente', 'success')
    cancelarEdicion()
    await obtenerTipos()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al crear tipo de socio'
    Swal.fire('Error', msg, 'error')
    console.error(error)
  }
}

async function actualizarTipo() {
  try {
    await axios.patch(`http://localhost:8081/sociosboca/${tipoEditandoId.value}`, {
      tipoSocioBocaId: tipoEditandoId.value,
      nombre: nuevoTipo.value.nombre
    })
    Swal.fire('Actualizado', 'Tipo de socio Boca actualizado correctamente', 'success')
    cancelarEdicion()
    await obtenerTipos()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al actualizar tipo de socio'
    Swal.fire('Error', msg, 'error')
    console.error(error)
  }
}

async function eliminarTipo(id) {
  const confirm = await Swal.fire({
    title: '¿Eliminar tipo de socio Boca?',
    text: 'Esta acción no se puede deshacer',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6'
  })

  if (!confirm.isConfirmed) return

  try {
    await axios.delete(`http://localhost:8081/sociosboca/${id}`)
    Swal.fire('Eliminado', 'Tipo de socio eliminado correctamente', 'success')
    await obtenerTipos()
  } catch (error) {
    const msg = error.response?.data?.error || 'Error al eliminar tipo'
    Swal.fire('Error', msg, 'error')
    console.error(error)
  }
}

onMounted(obtenerTipos)
</script>

<style scoped>
/* Mismos estilos, sin cambios */
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