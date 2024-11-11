import { NextUIProvider } from '@nextui-org/react';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { SocketProvider } from './components/SocketProvider';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <NextUIProvider>
      <SocketProvider>
        <App />
      </SocketProvider>
    </NextUIProvider>
  </StrictMode>,
);
