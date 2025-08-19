import { createRouter, createWebHistory } from 'vue-router';
// Asegúrate de usar los nombres correctos para tus vistas

import LoginView from '@/views/Login.vue'
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import PantallaPrincipal from '@/views/PantallaPrincipal.vue';
import Socios from '@/views/Socios.vue'
import CrearSocio from '@/views/CrearSocio.vue';
import Movimientos from '@/views/Movimientos.vue';
import CuotasPendientes from '@/views/CuotasPendientes.vue';
import ViajesBombonera from '@/views/ViajesBombonera.vue';
import VerViajesBombonera from '@/views/VerViajesBombonera.vue';
// import PagosViajes from '@/views/PagosViajes.vue';
import CrearSalones from '@/views/CrearSalones.vue';
import Alquileres from '@/views/Alquileres.vue';
//import ListaAlquileresView from '@/views/ListaAlquileres.vue'
import CalendarioAlquileres from '@/views/CalendarioAlquileres.vue'
import SociosBaja from '@/views/SociosBaja.vue';
import EditarSocio from '@/views/EditarSocio.vue';
import ActualizarSocioPeña from '@/views/ActualizarSocioPeña.vue';
import ActualizarSocioBoca from '@/views/ActualizarSocioBoca.vue';
import Cobradores from '@/views/Cobradores.vue';
import CuotasPorCobrador from '@/views/CuotasPorCobrador.vue';
import Beneficios from '@/views/Beneficios.vue';

const routes = [

  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  { path: '/principal', component: PantallaPrincipal },
  { path: '/socios', component: Socios },
  { path: '/nuevo-socio', component: CrearSocio },
  {path:'/movimientos', component: Movimientos},
  {path:'/cuotas-pendientes', component: CuotasPendientes},
  { path: '/viajesBombonera', component: ViajesBombonera }, 
  { path: '/viajes', component: VerViajesBombonera},
  // {path:'/pagos', component: PagosViajes},
  {path:'/salones', component: CrearSalones},
  {path:'/alquiler', component: Alquileres},
  {path: '/alquileres/calendario', component: CalendarioAlquileres},
  {path: "/socios-baja", component: SociosBaja},
  {path: '/socios/editar/:id',  component: EditarSocio},
  {path: '/sociospeña', component: ActualizarSocioPeña},
  {path: '/sociosboca', component: ActualizarSocioBoca},
  {path:'/cobradores', component: Cobradores},
  {path:'/cuotas-cobrador', component: CuotasPorCobrador},
  {path:'/beneficios', component: Beneficios}


];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
