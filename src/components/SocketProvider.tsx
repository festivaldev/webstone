import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
  type MutableRefObject,
  type ReactNode,
} from 'react';
import useStateRef from 'react-usestateref';
import { createStore, useStore, type StoreApi } from 'zustand';
import { useShallow } from 'zustand/shallow';

interface SocketStateType {
  hostname: string;
  port: string;
  socketPassphrase: string;
  useSecureSocket: boolean;
  blockListId: string;
  blockListPassphrase: string;

  setHostname: (hostname: string) => void;
  setPort: (port: string) => void;
  setSocketPassphrase: (socketPassphrase: string) => void;
  setUseSecureSocket: (useSecureSocket: boolean) => void;
  setBlockListId: (blockListId: string) => void;
  setBlockListPassphrase: (blockListPassphrase: string) => void;
}

interface SocketContextType {
  socket: WebSocket | null;
  readyState: number;
  isConnecting: boolean;
  isConnected: boolean;
  isSubscribed: boolean;
  eventHandlers: MutableRefObject<{
    [key: string]: ((event: Event) => void)[];
  }>;
  store: StoreApi<SocketStateType>;
  setIsSubscribed: React.Dispatch<React.SetStateAction<boolean>>;
  connect: () => void;
  disconnect: () => void;
  send: (type: string, payload: any) => void;
  subscribeToBlockList: () => void;
  unsubscribeFromBlockList: () => void;

  setBlockState: (blockId: string, powered: boolean) => void;
  setBlockPower: (blockId: string, power: number) => void;
  renameBlock: (blockId: string, name: string) => void;
  deleteBlock: (blockId: string) => void;
  changeBlockGroup: (blockId: string, groupId: string) => void;
  createGroup: (name: string) => void;
  renameGroup: (groupId: string, name: string) => void;
  deleteGroup: (groupId: string) => void;
  changeBlockIndex: (blockId: string, newIndex: number) => void;
  changeGroupIndex: (groupId: string, newIndex: number) => void;
}

export const DEFAULT_HOSTNAME = '127.0.0.1';
export const DEFAULT_PORT = '4321';
export const PUBLIC_BLOCK_LIST = '00000000-0000-0000-0000-000000000000';

const SocketStore = createStore<SocketStateType>((set) => ({
  hostname: localStorage.getItem('hostname') || '',
  port: localStorage.getItem('port') || '',
  socketPassphrase: '',
  useSecureSocket: location.href.startsWith('https'),
  blockListId: '',
  blockListPassphrase: '',

  setHostname: (hostname: string) => set({ hostname }),
  setPort: (port: string) => set({ port }),
  setSocketPassphrase: (socketPassphrase: string) => set({ socketPassphrase }),
  setUseSecureSocket: (useSecureSocket: boolean) => set({ useSecureSocket }),
  setBlockListId: (blockListId: string) => set({ blockListId }),
  setBlockListPassphrase: (blockListPassphrase: string) => set({ blockListPassphrase }),
}));

const SocketContext = createContext<SocketContextType | undefined>(undefined);

export const SocketProvider = ({ children }: { children: ReactNode }) => {
  const [socket, setSocket, socketRef] = useStateRef<WebSocket | null>(null);
  const [readyState, setReadyState, readyStateRef] = useStateRef<number>(WebSocket.CLOSED);

  const isConnecting = useMemo<boolean>(() => readyState === WebSocket.CONNECTING, [readyState]);
  const isConnected = useMemo<boolean>(() => readyState === WebSocket.OPEN, [readyState]);
  const [isSubscribed, setIsSubscribed] = useState<boolean>(false);

  const eventHandlers = useRef<{ [key: string]: Array<(event: Event) => void> }>({
    open: [],
    close: [],
    message: [],
    error: [],
    send: [],
  });

  const [store] = useState(() => SocketStore);

  const connect = useCallback(() => {
    const { hostname, port, useSecureSocket } = store.getState();

    const trimmedHostname = hostname?.trim().length ? hostname?.trim() : undefined;
    const trimmedPort = port?.trim().length ? port?.trim() : undefined;

    if (trimmedHostname) {
      localStorage.setItem('hostname', trimmedHostname);
    } else {
      localStorage.removeItem('hostname');
    }

    if (trimmedPort) {
      localStorage.setItem('port', trimmedPort);
    } else {
      localStorage.removeItem('port');
    }

    const newSocket = new WebSocket(
      `${!useSecureSocket ? 'ws' : 'wss'}://${trimmedHostname || DEFAULT_HOSTNAME}:${trimmedPort || DEFAULT_PORT}`,
    );

    newSocket.onopen = (event) => {
      setReadyState(() => newSocket.readyState);
      eventHandlers.current.open.forEach((handler) => handler(event));
    };

    newSocket.onclose = (event) => {
      setReadyState(() => newSocket.readyState);
      eventHandlers.current.close.forEach((handler) => handler(event));

      setSocket(() => null);
    };

    newSocket.onmessage = (event) => {
      let { data } = event;

      try {
        data = JSON.parse(data);
      } catch {}

      const newEvent = new MessageEvent('message', {
        bubbles: event.bubbles,
        cancelable: event.cancelable,
        composed: event.composed,
        data,
        lastEventId: event.lastEventId,
        origin: event.origin,
        source: event.source,
      });

      eventHandlers.current.message.forEach((handler) => handler(newEvent));
    };

    newSocket.onerror = (event) => {
      eventHandlers.current.error.forEach((handler) => handler(event));
    };

    setSocket(() => newSocket);
    setReadyState(newSocket.readyState);
  }, []);

  const disconnect = useCallback(() => {
    socket?.close(1000);
  }, [socket]);

  const sendData = useCallback(
    (data: any) => {
      if (!socketRef.current || readyStateRef.current !== WebSocket.OPEN) return;

      eventHandlers.current.send.forEach((handler) => handler(new CustomEvent('send', { detail: data })));

      if (typeof data === 'object') {
        data = JSON.stringify(data);
      }
      socketRef.current?.send(data);
    },
    [socket, readyState],
  );

  const send = useCallback(
    (type: string, payload: any) => {
      sendData({
        type,
        payload,
      });
    },
    [socket, readyState],
  );

  const subscribeToBlockList = useCallback(() => {
    const { blockListId, blockListPassphrase } = store.getState();

    send('SUBSCRIBE', {
      registryId: blockListId,
      passphrase: blockListPassphrase,
    });
  }, []);

  const unsubscribeFromBlockList = useCallback(() => {
    const { blockListId } = store.getState();

    send('UNSUBSCRIBE', {
      registryId: blockListId,
    });
  }, []);

  const setBlockState = (blockId: string, powered: boolean) => {
    send('BLOCK_STATE', {
      blockId,
      powered,
    });
  };

  const setBlockPower = (blockId: string, power: number) => {
    send('BLOCK_POWER', {
      blockId,
      power: Math.min(Math.max(power, 0), 15),
    });
  };

  const renameBlock = (blockId: string, name: string) => {
    if (name?.trim().length) {
      send('RENAME_BLOCK', {
        blockId,
        name: name.substring(0, 64).trim(),
      });
    }
  };

  const deleteBlock = (blockId: string) => {
    send('UNREGISTER_BLOCK', {
      blockId,
    });
  };

  const changeBlockGroup = (blockId: string, groupId: string) => {
    send('CHANGE_BLOCK_GROUP', {
      blockId,
      groupId,
    });
  };

  const createGroup = (name: string) => {
    send('CREATE_GROUP', {
      name,
    });
  };

  const renameGroup = (groupId: string, name: string) => {
    send('RENAME_GROUP', {
      groupId,
      name,
    });
  };

  const deleteGroup = (groupId: string) => {
    send('DELETE_GROUP', {
      groupId,
    });
  };

  const changeBlockIndex = (blockId: string, newIndex: number) => {
    send('CHANGE_BLOCK_INDEX', {
      id: blockId,
      newIndex,
    });
  };

  const changeGroupIndex = (groupId: string, newIndex: number) => {
    send('CHANGE_GROUP_INDEX', {
      id: groupId,
      newIndex,
    });
  };

  return (
    <SocketContext.Provider
      value={{
        socket,
        readyState,
        isConnecting,
        isConnected,
        isSubscribed,
        eventHandlers,
        store,
        setIsSubscribed,
        connect,
        disconnect,
        send,
        subscribeToBlockList,
        unsubscribeFromBlockList,

        setBlockState,
        setBlockPower,
        renameBlock,
        deleteBlock,
        changeBlockGroup,
        createGroup,
        renameGroup,
        deleteGroup,
        changeBlockIndex,
        changeGroupIndex,
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => {
  const context = useContext(SocketContext);

  if (!context) {
    throw new Error('useSocket must be used within a SocketProvider');
  }

  return context;
};

export const useSocketStore = () => {
  const context = useContext(SocketContext);

  if (!context) {
    throw new Error('useForm must be used within a FormProvider');
  }

  return useStore(
    SocketStore,
    useShallow((state) => state),
  );
};

export const useSocketListeners = () => {
  const context = useContext(SocketContext);
  if (!context) {
    throw new Error('useSocketListeners must be used within a SocketProvider');
  }

  const { eventHandlers } = context;

  const addEventListener = useCallback(
    (eventType: 'open' | 'close' | 'message' | 'error' | 'send', handler: (event: Event) => void) => {
      eventHandlers.current[eventType].push(handler);
    },
    [context],
  );

  const removeEventListener = useCallback(
    (eventType: 'open' | 'close' | 'message' | 'error' | 'send', handler: (event: Event) => void) => {
      eventHandlers.current[eventType] = context.eventHandlers.current[eventType].filter((h) => h !== handler);
    },
    [context],
  );

  return { addEventListener, removeEventListener };
};
