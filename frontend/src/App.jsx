import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import PortalLayout from './pages/PortalLayout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import RequestForm from './pages/RequestForm.jsx';
import MyRequests from './pages/MyRequests.jsx';
import Assignments from './pages/Assignments.jsx';
import Management from './pages/Management.jsx';
import Profile from './pages/Profile.jsx';
import WasteGuide from './pages/WasteGuide.jsx';
import Issues from './pages/Issues.jsx';
import IssueManagement from './pages/IssueManagement.jsx';
import CollectionCalendar from './pages/CollectionCalendar.jsx';
function StartRoute(){const {user}=useAuth();return <Navigate to={user?'/app':'/'} replace/>;}
export default function App(){return <Routes><Route path="/" element={<Home/>}/><Route path="/guide" element={<WasteGuide/>}/><Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/><Route element={<ProtectedRoute/>}><Route path="/app" element={<PortalLayout/>}><Route index element={<Dashboard/>}/><Route path="guide" element={<WasteGuide/>}/><Route element={<ProtectedRoute roles={['CITIZEN']}/>}><Route path="calendar" element={<CollectionCalendar/>}/><Route path="requests/new" element={<RequestForm/>}/><Route path="requests" element={<MyRequests/>}/><Route path="issues" element={<Issues/>}/></Route><Route element={<ProtectedRoute roles={['COLLECTOR']}/>}><Route path="assignments" element={<Assignments/>}/></Route><Route element={<ProtectedRoute roles={['ADMIN']}/>}><Route path="manage" element={<Management/>}/><Route path="issue-queue" element={<IssueManagement/>}/></Route><Route path="profile" element={<Profile/>}/></Route></Route><Route path="*" element={<StartRoute/>}/></Routes>;}
