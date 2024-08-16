import { BlockButton, Header } from '@/components';
import classNames from '@/utilities/classNames';
import { XCircleIcon } from '@heroicons/react/24/outline';
import { Button, Checkbox, Input, Tooltip } from '@nextui-org/react';
import { useEffect, useMemo, useState } from 'react';

const DEFAULT_HOSTNAME = '127.0.0.1';
const DEFAULT_PORT = 4321;

const App = (): React.ReactNode => {
  const isSecureConnection = useMemo(() => location.protocol.startsWith('https'), []);

  const [hostname, setHostname] = useState<string | undefined>(undefined);
  const [port, setPort] = useState<number | undefined>(undefined);
  const [useSecureSocket, setUseSecureSocket] = useState<boolean>(isSecureConnection);

  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [isConnecting, setConnecting] = useState<boolean>(false);
  const [isConnected, setConnected] = useState<boolean>(false);
  const [connectError, setConnectError] = useState<string | undefined>(undefined);

  const [blocks, setBlocks] = useState<Block[]>([
    // {
    //   blockId: '0',
    //   name: 'debug',
    //   power: 15,
    //   powered: true,
    // },
  ]);

  useEffect(() => {
    setHostname(() => localStorage.getItem('hostname') ?? undefined);
    setPort(() => (localStorage.getItem('port') ? Number(localStorage.getItem('port')) : undefined));
  }, []);

  const trimmedHostname = useMemo(() => (hostname?.trim().length ? hostname?.trim() : undefined), [hostname]);
  const trimmedPort = useMemo(() => (port ? String(port).trim() : undefined), [port]);

  const connect = () => {
    const connectUri = `${isSecureConnection || useSecureSocket ? 'wss' : 'ws'}://${trimmedHostname ?? DEFAULT_HOSTNAME}:${trimmedPort ?? DEFAULT_PORT}`;
    const _socket = new WebSocket(connectUri);

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

    setConnectError(() => undefined);
    setConnecting(() => true);

    _socket.addEventListener('open', () => {
      console.log('Connection opened');
      setConnected(() => true);
    });

    _socket.addEventListener('close', () => {
      console.log('Connection closed');
      setSocket(() => null);

      setConnecting(() => false);
      setConnected(() => false);
    });

    _socket.addEventListener('error', (_) => {
      setConnectError(() => `Failed to connect to ${connectUri}!`);
    });

    _socket.addEventListener('message', ({ data: message }) => {
      try {
        const data = JSON.parse(message);

        switch (data.type) {
          case 'block_list':
            setBlocks(() => data.data);
            break;
          case 'block_state':
            setBlocks((blocks) =>
              blocks.map((block) =>
                block.blockId === data.data.blockId ? { ...block, powered: data.data.powered } : block,
              ),
            );
            break;
          case 'block_power':
            setBlocks((blocks) =>
              blocks.map((block) =>
                block.blockId === data.data.blockId ? { ...block, power: data.data.power } : block,
              ),
            );
            break;
          case 'rename_block':
            setBlocks((blocks) =>
              blocks.map((block) => (block.blockId === data.data.blockId ? { ...block, name: data.data.name } : block)),
            );
            break;
          default:
            break;
        }
      } catch (error) {
        console.error('Failed to parse JSON message', error);
      }
    });

    setSocket(() => _socket);
  };

  const disconnect = () => {
    socket?.close();
  };

  const setBlockState = (blockId: string, powered: boolean): void => {
    socket?.send(
      JSON.stringify({
        type: 'block_state',
        data: {
          blockId,
          powered,
        },
      }),
    );
  };

  const setBlockPower = (blockId: string, power: number): void => {
    socket?.send(
      JSON.stringify({
        type: 'block_power',
        data: {
          blockId,
          power: Math.min(Math.max(power, 0), 15),
        },
      }),
    );
  };

  const renameBlock = (blockId: string): void => {
    const blockName = prompt('Rename the button to:');
    if (blockName?.trim().length) {
      socket?.send(
        JSON.stringify({
          type: 'rename_block',
          data: {
            blockId,
            name: blockName.substring(0, 64).trim(),
          },
        }),
      );
    }
  };

  const unregisterBlock = (blockId: string): void => {
    if (confirm('Do you really want to delete this block?')) {
      socket?.send(
        JSON.stringify({
          type: 'unregister_block',
          data: {
            blockId,
          },
        }),
      );
    }
  };

  return (
    <div className="max-h-full w-full overflow-y-scroll p-4 pt-20">
      <Header isConnected disconnect={disconnect} />

      {!isConnected && (
        <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              connect();
            }}
            className="space-y-4"
          >
            <div className="flex w-full flex-nowrap gap-4 md:flex-nowrap">
              <Input
                type="text"
                label="Hostname / IP address"
                placeholder={DEFAULT_HOSTNAME}
                value={hostname ?? ''}
                isDisabled={isConnecting}
                onChange={(e) => setHostname(() => e.target.value)}
              />

              <Input
                type="string"
                label="Port"
                placeholder={`${DEFAULT_PORT}`}
                value={String(port ?? '')}
                max={65535}
                isDisabled={isConnecting}
                className="w-24 text-center"
                onChange={(e) => !isNaN(parseInt(e.target.value, 10)) && setPort(() => Number(e.target.value))}
              />
            </div>

            <Tooltip
              showArrow
              closeDelay={300}
              content={
                <>
                  <p className="text-center">
                    You can't connect to insecure WebSockets
                    <br />
                    because this site is using HTTPS.
                  </p>
                  <a
                    className="mt-2 text-center text-primary hover:underline"
                    href={`http://${location.href.replace(/http(s)?:\/\//, '')}`}
                  >
                    Switch to HTTP
                  </a>
                </>
              }
              hidden={!isSecureConnection}
            >
              <div>
                <Checkbox
                  isSelected={isSecureConnection || useSecureSocket}
                  isDisabled={isSecureConnection || isConnecting}
                  onChange={(e) => setUseSecureSocket(e.target.checked)}
                  disableAnimation={isSecureConnection}
                >
                  Use Secure WebSocket
                </Checkbox>
              </div>
            </Tooltip>

            <div className="flex justify-center gap-4">
              <Button
                color="primary"
                isDisabled={isConnecting}
                isLoading={isConnecting}
                type="submit"
                className="flex-1"
              >
                {isConnecting ? `Connecting...` : 'Connect to Minecraft'}
              </Button>

              <Button isIconOnly variant="flat" isDisabled={!isConnecting} onClick={disconnect}>
                <XCircleIcon className="size-6 text-danger" />
              </Button>
            </div>

            <span
              className={classNames('block text-center text-xs text-danger', {
                invisible: !connectError && false,
              })}
            >
              {connectError || <>&nbsp;</>}
            </span>
          </form>
        </div>
      )}

      {isConnected && blocks.length === 0 && (
        <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
          <p className="text-center text-xl text-gray-500">
            No Webstone blocks registered.
            <br />
            <span className="text-base">Place a Webstone block and right-click it with an empty hand to register.</span>
          </p>
        </div>
      )}

      {isConnected && blocks.length > 0 && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 2xl:grid-cols-8">
          {blocks.map((block) => (
            <BlockButton
              key={block.blockId}
              block={block}
              setBlockState={setBlockState}
              setBlockPower={setBlockPower}
              renameBlock={renameBlock}
              unregisterBlock={unregisterBlock}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default App;
