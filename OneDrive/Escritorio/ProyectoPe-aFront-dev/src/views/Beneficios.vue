<template>
  <div class="container py-3">
    <!-- Título y total -->
    <header class="page-header mb-4">
      <h1>Beneficios</h1>
      <p class="total-socios">
        Total de beneficios: <strong>{{ totalBeneficios }}</strong>
      </p>
    </header>

    <!-- Filtro -->
    <div class="row align-items-center mb-4 g-2">
      <div class="col-md">
        <input
          v-model="filtro"
          type="text"
          class="form-control shadow-sm"
          placeholder="Buscar por nombre, apellido o DNI"
          aria-label="Buscar beneficios"
          @input="cambiarPagina(1)"
        />
      </div>
            <div class="col-auto">
         <button
          @click="recalcularBeneficios"
          class="btn-limpiar"
          :disabled="loading"
          title="Actualizar"
        >
          <i :class="['bi', 'bi-arrow-repeat', { 'spin': loading }]"></i>
        </button>
      </div>
    </div>
   

    <!-- Tabla -->
    <div class="table-responsive shadow-sm rounded">
      <table class="table professional-table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>DNI</th>
            <th>Fecha otorgado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="beneficio in beneficiosPagina" :key="beneficio.beneficiosId">
            <td>{{ beneficio.nombre || "—" }}</td>
            <td>{{ beneficio.apellido || "—" }}</td>
            <td>{{ beneficio.dni || "—" }}</td>
            <td>{{ formatoFecha(beneficio.fechaOtorgado) }}</td>
          </tr>
          <tr v-if="!beneficiosPagina.length">
            <td colspan="5" class="text-center text-muted py-3">
              No se encontraron beneficios.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Paginación -->
    <nav aria-label="Paginación beneficios" class="mt-3 d-flex justify-content-center">
      <ul class="pagination mb-0">
        <li
          class="page-item"
          :class="{ disabled: paginaActual === 1 }"
          @click.prevent="cambiarPagina(paginaActual - 1)"
        >
          <a class="page-link" href="#" tabindex="-1">
            <i class="bi bi-chevron-left"></i> Anterior
          </a>
        </li>

        <li
          v-for="num in totalPaginas"
          :key="num"
          class="page-item"
          :class="{ active: num === paginaActual }"
          @click.prevent="cambiarPagina(num)"
        >
          <a class="page-link" href="#">{{ num }}</a>
        </li>

        <li
          class="page-item"
          :class="{ disabled: paginaActual === totalPaginas }"
          @click.prevent="cambiarPagina(paginaActual + 1)"
        >
          <a class="page-link" href="#">
            Siguiente <i class="bi bi-chevron-right"></i>
          </a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import axios from "axios";
import Swal from 'sweetalert2'

const API = "http://127.0.0.1:8081/beneficios"; // Cambia la URL si es necesario

const beneficios = ref([]);
const totalBeneficios = ref(0);
const filtro = ref("");
const paginaActual = ref(1);
const beneficiosPorPagina = 10;
const loading = ref(false);
const totalPaginas = computed(() =>
  Math.ceil(totalBeneficios.value / beneficiosPorPagina)
);

const beneficiosPagina = computed(() => beneficios.value || []);
async function cargarBeneficios() {
  try {
    const params = {
      pagina: paginaActual.value,
      limite: beneficiosPorPagina,
    };

    if (filtro.value.trim() !== "") {
      params.filtro = filtro.value.trim();
    }

    const res = await axios.get(API, { params });
    beneficios.value = res.data.beneficios || [];
    totalBeneficios.value = res.data.total || 0;

    // Si la página actual está fuera del rango por algún cambio, volver a la 1
    if (beneficios.value.length === 0 && paginaActual.value > 1) {
      paginaActual.value = 1;
      cargarBeneficios();
    }
  } catch (error) {
    console.error("Error al cargar beneficios:", error);
    beneficios.value = [];
    totalBeneficios.value = 0;
  }
  finally{
    loading.value = false;
  }
}
async function recalcularBeneficios() {
  try {
    const response = await axios.post('http://localhost:8081/beneficios/recalcular')
    if (response.status === 200) {
      Swal.fire({
        icon: 'success',
        title: '¡Éxito!',
        text: response.data.mensaje || 'Beneficios recalculados correctamente',
      })
      await cargarBeneficios()
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: error.response?.data?.error || 'Error al recalcular beneficios',
    })
  }
  finally{
    loading.value = false;
  }
}


function cambiarPagina(num) {
  if (num < 1 || num > totalPaginas.value) return;
  paginaActual.value = num;
}

function formatoFecha(fechaISO) {
  if (!fechaISO) return "No disponible";
  const date = new Date(fechaISO);
  return date.toLocaleDateString();
}

watch([paginaActual, filtro], cargarBeneficios);

onMounted(cargarBeneficios);
</script>


<style scoped>


/* --- Título --- */
.page-header h1 {
  font-size: 2.8rem;
  font-weight: 900;
  color: #003366;
  text-align: center;
  margin-bottom: 0.2rem;
  letter-spacing: 0.04em;
  user-select: none;
  position: relative;
}

.page-header h1::after {
  content: "";
  display: block;
  width: 140px;
  height: 4px;
  background: #ffc107; /* Amarillo Boca */
  margin: 0.25rem auto 0;
  border-radius: 2px;
}

.total-socios {
  font-size: 1.1rem;
  color: #2c3e50cc;
  font-style: italic;
  text-align: center;
  margin-top: 0;
  user-select: none;
}

/* --- Tabla --- */
/* Tabla principal */
.professional-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  font-size: 0.95rem;
}

/* Cabecera */
.professional-table thead th {
  padding: 14px 16px;
  text-align: center;
  font-weight: 700;
  background-color: #002b5b; /* Azul fuerte */
  color: #fff;
  white-space: nowrap;
}

/* Celdas */
.professional-table td {
  padding: 12px 16px;
  text-align: center;
  vertical-align: middle;
  border-top: 1px solid #eee;
}

/* Hover en filas */
.professional-table tbody tr:hover {
  background-color: #f8f9fa;
}

/* Botones solo iconos */
.icon-btn {
  background: transparent;
  border: none;
  padding: 5px;
  cursor: pointer;
  color: #003366;
  font-size: 1.2rem;
  transition: color 0.25s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  color: #00509e;
}

.icon-info:hover {
  color: #007bff;
}

.icon-warning:hover {
  color: #ffc107;
}

.icon-danger:hover {
  color: #dc3545;
}

/* Botón Nuevo Socio */
.btn-boca-primary {
  background-color: #003366; /* Azul Boca */
  color: #ffc107; /* Amarillo Boca */
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

.btn-boca-primary i {
  font-size: 1.25rem;
}

.btn-boca-primary:hover,
.btn-boca-primary:focus {
  background-color: #00509e; /* Azul Boca más claro */
  color: #fff;
  outline: none;
}

/* Input filtro */
input.form-control {
  border-radius: 0.375rem;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.1);
  transition: box-shadow 0.3s ease;
}

input.form-control:focus {
  box-shadow: 0 0 8px #00509e;
  border-color: #00509e;
  outline: none;
}

/* Paginación */
.pagination {
  user-select: none;
}

.page-item.disabled .page-link {
  cursor: not-allowed;
  color: #6c757d;
  background-color: transparent;
  border-color: transparent;
}

.page-item.active .page-link {
  z-index: 1;
  color: #ffffff;
  background-color: #003366;
  border-color: #003366;
  box-shadow: 0 0 10px rgb(0 51 102 / 0.7);
}

.page-link {
  color: #003366;
  border-radius: 0.375rem;
  padding: 0.35rem 0.75rem;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-link:hover:not(.active) {
  background-color: #dcefff;
  color: #003366;
}

/* --- Modal --- */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}

.modal-container {
  background: white;
  border-radius: 0.5rem;
  width: 90%;
  max-width: 550px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgb(0 0 0 / 0.25);
  padding: 1.5rem 2rem;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #003366;
  margin-bottom: 1rem;
}

.modal-header h2 {
  font-weight: 700;
  color: #003366;
  font-size: 1.8rem;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #003366;
  line-height: 1;
}

.modal-content {
  font-size: 1rem;
  color: #212529;
}

.modal-section {
  margin-bottom: 1.5rem;
}

.modal-section h3 {
  font-weight: 700;
  border-bottom: 2px solid #ffc107;
  padding-bottom: 0.25rem;
  margin-bottom: 1rem;
  color: #003366;
  user-select: none;
}

.modal-section p {
  margin: 0.3rem 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #495057;
}

.activo {
  color: #198754;
  font-weight: 700;
}

.inactivo {
  color: #dc3545;
  font-weight: 700;
}

/* Fade para modal */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.btn-limpiar,
.btn-imprimir-todos {
  background-color: #003366;
  color: #ffc107;
  border: none;
  padding: 10px 16px;
  border-radius: 0.5rem;
  font-weight: bold;
  font-size: 1rem;
  cursor: pointer;
  transition: 0.3s ease;
}

.btn-limpiar:hover,
.btn-imprimir-todos:hover {
  background-color: #00509e;
  color: white;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>