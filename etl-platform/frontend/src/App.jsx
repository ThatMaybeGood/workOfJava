import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Datasource from './pages/Datasource';
import Task from './pages/Task';
import Pipeline from './pages/Pipeline';
import Mapping from './pages/Mapping';
import Log from './pages/Log';
import Settings from './pages/Settings';

function PrivateRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="datasource" element={<Datasource />} />
        <Route path="pipeline" element={<Pipeline />} />
        <Route path="task" element={<Task />} />
        <Route path="mapping" element={<Mapping />} />
        <Route path="log" element={<Log />} />
        <Route path="settings" element={<Settings />} />
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}
