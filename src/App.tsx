import { BlockGroup, ConnectionScreen, Header } from '@/components';
import { Spinner } from '@nextui-org/react';
import React from 'react';
import Modals, { type ModalHandler } from './components/Modals';
import SocketClient from './utilities/SocketClient';

const App = (): React.ReactNode => {
  const [socket, setSocket] = React.useState<SocketClient>();

  const [readyState, setReadyState] = React.useState<number>(-1);
  const [connectionError, setConnectionError] = React.useState<string | undefined>(undefined);
  const isConnecting = React.useMemo(() => readyState === WebSocket.CONNECTING, [readyState]);
  const isConnected = React.useMemo(() => readyState === WebSocket.OPEN, [readyState]);

  const [blockGroups, setBlockGroups] = React.useState<BlockGroup[] | undefined>([]);
  const [blocks, setBlocks] = React.useState<Block[] | undefined>();

  const ungroupedBlocks = React.useMemo<Block[]>(() => blocks?.filter((block) => !block.groupId) ?? [], [blocks]);
  const modals = React.useRef<ModalHandler>(null);

  React.useEffect(() => {
    if (!socket) {
      const _socket = new SocketClient();
      _socket.addEventListener('readyStateChange', (e: Event) => {
        setReadyState(() => (e as CustomEvent).detail);
      });

      _socket.addEventListener('message', (e: Event) => {
        const { data } = e as MessageEvent;

        if (typeof data === 'object') {
          switch (data.type) {
            case 'block_groups':
              setBlockGroups(() => data.data);
              break;
            case 'block_list':
              setBlocks(() => data.data);
              break;
            case 'block_state':
              setBlocks((blocks) =>
                blocks?.map((block) =>
                  block.blockId === data.data.blockId ? { ...block, powered: data.data.powered } : block,
                ),
              );
              break;
            case 'block_power':
              setBlocks((blocks) =>
                blocks?.map((block) =>
                  block.blockId === data.data.blockId ? { ...block, power: data.data.power } : block,
                ),
              );
              break;
            case 'rename_block':
              setBlocks((blocks) =>
                blocks?.map((block) =>
                  block.blockId === data.data.blockId ? { ...block, name: data.data.name } : block,
                ),
              );
              break;
            default:
              break;
          }
        }
      });

      _socket.addEventListener('close', (e: Event) => {
        const event = e as CloseEvent;

        switch (event.code) {
          case 1000:
            break;
          case 1001:
            setConnectionError(() => 'Connection closed by server.');
            break;
          case 1006:
            setConnectionError(() => `Connection to ${_socket.connectionUri} failed.`);
            break;
          default:
            break;
        }

        setBlockGroups(() => undefined);
        setBlocks(() => undefined);
      });

      setSocket(() => _socket);
    }
  }, []);

  return (
    <>
      <div className="p-4 pt-20">
        <Header
          isConnected={isConnected}
          onCreateGroup={() => modals.current?.openCreateGroupModal()}
          onReorderGroups={() => modals.current?.openChangeGroupOrderModal()}
          onDisconnect={() => {
            socket?.disconnect();
          }}
        />

        {!isConnected && (
          <ConnectionScreen
            isConnecting={isConnecting}
            connectionError={connectionError}
            onConnect={(hostname, port, passphrase, useSecureSocket) => {
              setConnectionError(() => undefined);
              socket?.connect(hostname, port, passphrase, useSecureSocket);
            }}
            onCancelConnect={() => {
              socket?.disconnect();
            }}
          />
        )}

        {isConnected && (
          <>
            {(!blockGroups || !blocks) && (
              <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
                <Spinner size="lg" />
              </div>
            )}

            {blockGroups?.length === 0 && blocks?.length === 0 && (
              <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
                <p className="text-center text-xl text-gray-500">
                  No Webstone blocks registered.
                  <br />
                  <span className="text-base">
                    Place a Webstone block and right-click it with an empty hand to register.
                  </span>
                </p>
              </div>
            )}

            <div className="space-y-4">
              {ungroupedBlocks && ungroupedBlocks.length > 0 && (
                <BlockGroup
                  blocks={ungroupedBlocks}
                  onBlockStateChanged={(blockId, powered) => socket?.setBlockState(blockId, powered)}
                  onBlockPowerChanged={(blockId, power) => socket?.setBlockPower(blockId, power)}
                  onBlockRename={(blockId) => modals.current?.openRenameBlockModal(blockId)}
                  onBlockGroupChange={(blockId) => modals.current?.openChangeBlockGroupModal(blockId)}
                  onBlockDelete={(blockId) => modals.current?.openDeleteBlockModal(blockId)}
                  onGroupRename={(groupId) => modals.current?.openRenameGroupModal(groupId)}
                  onGroupDelete={(groupId) => modals.current?.openDeleteGroupModal(groupId)}
                />
              )}

              {blockGroups?.map((blockGroup) => (
                <BlockGroup
                  key={blockGroup.groupId}
                  group={blockGroup}
                  blocks={
                    (blockGroup.blockIds
                      .map((id) => blocks?.find((block) => id === block.blockId))
                      .filter(Boolean) as Block[]) ?? []
                  }
                  onBlockStateChanged={(blockId, powered) => socket?.setBlockState(blockId, powered)}
                  onBlockPowerChanged={(blockId, power) => socket?.setBlockPower(blockId, power)}
                  onBlockRename={(blockId) => modals.current?.openRenameBlockModal(blockId)}
                  onBlockGroupChange={(blockId) => modals.current?.openChangeBlockGroupModal(blockId)}
                  onBlockDelete={(blockId) => modals.current?.openDeleteBlockModal(blockId)}
                  onGroupRename={(groupId) => modals.current?.openRenameGroupModal(groupId)}
                  onReorderBlocks={(groupId) => modals.current?.openChangeBlockOrderModal(groupId)}
                  onGroupDelete={(groupId) => modals.current?.openDeleteGroupModal(groupId)}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {blockGroups && blocks && <Modals ref={modals} socket={socket} blockGroups={blockGroups} blocks={blocks} />}
    </>
  );
};

export default App;
