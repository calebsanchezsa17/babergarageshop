import React from 'react';
import {Navigate,Route,Routes} from 'react-router-dom';
import {useAuth} from './context/AuthContext';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/AdminDashboard';
import BarbersPage from './pages/BarbersPage';
import RegisterBarberPage from './pages/RegisterBarberPage';
import MonitoringPage from './pages/MonitoringPage';
import SettingsPage from './pages/SettingsPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import BarberDashboard from './pages/BarberDashboard';
import OnboardingPage from './pages/OnboardingPage';

function Guard({role,children}){const{user,loading}=useAuth();if(loading)return <div className="center-screen"><div className="loader"/></div>;if(!user)return <Navigate to="/login" replace/>;if(role&&user.role!==role)return <Navigate to={user.role==='ADMIN'?'/admin':'/barber'} replace/>;return children}
export default function App(){return <Routes>
 <Route path="/login" element={<LoginPage/>}/>
 <Route path="/onboarding" element={<Guard role="BARBER"><OnboardingPage/></Guard>}/>
 <Route path="/" element={<Guard><Layout/></Guard>}>
   <Route index element={<Navigate to="/home" replace/>}/>
   <Route path="home" element={<HomeRedirect/>}/>
   <Route path="admin" element={<Guard role="ADMIN"><AdminDashboard/></Guard>}/>
   <Route path="admin/barbers" element={<Guard role="ADMIN"><BarbersPage/></Guard>}/>
   <Route path="admin/register" element={<Guard role="ADMIN"><RegisterBarberPage/></Guard>}/>
   <Route path="admin/monitoring" element={<Guard role="ADMIN"><MonitoringPage/></Guard>}/>
   <Route path="admin/settings" element={<Guard role="ADMIN"><SettingsPage/></Guard>}/>
   <Route path="barber" element={<Guard role="BARBER"><BarberDashboard/></Guard>}/>
   <Route path="change-password" element={<ChangePasswordPage/>}/>
 </Route>
 <Route path="*" element={<Navigate to="/home" replace/>}/>
 </Routes>}
function HomeRedirect(){const{user}=useAuth();return <Navigate to={user?.role==='ADMIN'?'/admin':'/barber'} replace/>}
