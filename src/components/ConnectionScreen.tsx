import { classNames } from '@/utilities';
import { PowerIcon, XCircleIcon } from '@heroicons/react/24/outline';
import { Button, Checkbox, Input, Select, SelectItem, Tooltip } from '@nextui-org/react';
import React from 'react';
import {
  DEFAULT_HOSTNAME,
  DEFAULT_PORT,
  PUBLIC_BLOCK_LIST,
  useSocket,
  useSocketListeners,
  useSocketStore,
} from './SocketProvider';

const ConnectionScreen = ({ blockLists }: { blockLists: { [key: string]: string } }): React.ReactNode => {
  const socket = useSocket();
  const socketListener = useSocketListeners();
  const socketStore = useSocketStore();

  const isSecureConnection = React.useMemo(() => location.protocol.startsWith('https'), []);

  const [errorMessage, setErrorMessage] = React.useState<string>();
  const [waitingForSubscription, setWaitingForSubscription] = React.useState<boolean>(false);

  React.useEffect(() => {
    const onMessage = (e: Event): void => {
      const { data } = e as MessageEvent;

      switch (data.type) {
        case 'AUTH_RES':
          if (data.payload.authorized) {
            setErrorMessage(() => undefined);
          } else {
            setErrorMessage(() => data.payload.message);
          }

          break;
        case 'SUBSCRIBE':
          if (data.payload.subscribed) {
            socket.store.getState().setSocketPassphrase('');
            socket.store.getState().setBlockListPassphrase('');
          } else {
            setErrorMessage(() => data.payload.message);
          }

          socket.setIsSubscribed(() => data.payload.subscribed);
          setWaitingForSubscription(() => false);
          break;
        default:
          break;
      }
    };

    const onError = (e: Event): void => {
      const { message } = e as ErrorEvent;

      setErrorMessage(() => message ?? 'Failed to connect to WebSocket server.');
    };

    socketListener.addEventListener('message', onMessage);
    socketListener.addEventListener('error', onError);

    return () => {
      socketListener.removeEventListener('message', onMessage);
      socketListener.removeEventListener('error', onError);
    };
  }, []);

  return (
    <div className="absolute bottom-0 left-0 right-0 top-0 flex flex-col items-center justify-center">
      <div className="space-y-4 rounded-xl border p-6 dark:border-slate-100/10 dark:bg-slate-950">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            setErrorMessage(() => undefined);
            socket.connect();
          }}
          className="space-y-4"
        >
          <ConnectionScreen.InputGroup>
            <Input
              type="text"
              label="Hostname / IP address"
              placeholder={DEFAULT_HOSTNAME}
              value={socketStore.hostname ?? ''}
              isDisabled={socket.isConnecting || socket.isConnected}
              onChange={(e) => socketStore.setHostname(e.target.value)}
            />

            <Input
              type="string"
              label="Port"
              placeholder={`${DEFAULT_PORT}`}
              value={socketStore.port ?? ''}
              max={65535}
              maxLength={5}
              isDisabled={socket.isConnecting || socket.isConnected}
              className="w-24 text-center"
              onChange={(e) => socketStore.setPort(e.target.value)}
            />
          </ConnectionScreen.InputGroup>

          <ConnectionScreen.InputGroup>
            <Input
              type="password"
              label="Passphrase"
              placeholder="Optional"
              value={socketStore.socketPassphrase ?? ''}
              isDisabled={socket.isConnecting || socket.isConnected}
              onChange={(e) => socketStore.setSocketPassphrase(e.target.value)}
            />
          </ConnectionScreen.InputGroup>

          <Tooltip
            showArrow
            closeDelay={300}
            content={
              <div className="max-w-64 space-y-3 text-center">
                <p>You can't connect to insecure WebSockets because this site is using HTTPS.</p>
                <a
                  className="block text-primary hover:underline"
                  href={`http://${location.href.replace(/http(s)?:\/\//, '')}`}
                >
                  Switch to HTTP
                </a>

                <small className="mx-4 block text-xs text-white/40">
                  Your browser may display a disclaimer that this site is insecure.
                </small>
              </div>
            }
            hidden={!isSecureConnection}
          >
            <div>
              <Checkbox
                isSelected={isSecureConnection || socketStore.useSecureSocket}
                isDisabled={isSecureConnection || socket.isConnecting || socket.isConnected}
                onChange={(e) => socketStore.setUseSecureSocket(e.target.checked)}
                disableAnimation={isSecureConnection}
              >
                Use Secure WebSocket
              </Checkbox>
            </div>
          </Tooltip>

          <div className="flex justify-center gap-4">
            <Button
              color="primary"
              isDisabled={socket.isConnecting || socket.isConnected}
              isLoading={socket.isConnecting}
              type="submit"
              className="flex-1"
            >
              {!socket.isConnected && !socket.isConnecting && 'Connect to Minecraft'}
              {socket.isConnecting && 'Connecting...'}
              {!socket.isConnecting && socket.isConnected && 'Connected'}
            </Button>

            <Tooltip
              showArrow
              closeDelay={300}
              content={socket.isConnecting ? 'Cancel' : 'Disconnect'}
              hidden={!socket.isConnecting && !socket.isConnected}
            >
              <div>
                <Button
                  className="text-danger"
                  isIconOnly
                  variant="flat"
                  isDisabled={!socket.isConnecting && !socket.isConnected}
                  onClick={(e) => {
                    e.preventDefault();
                    socket.disconnect();
                  }}
                >
                  {(socket.isConnecting || !socket.isConnected) && <XCircleIcon className="size-6" />}
                  {!socket.isConnecting && socket.isConnected && <PowerIcon className="size-6" />}
                </Button>
              </div>
            </Tooltip>
          </div>

          {!socket.isConnecting && !socket.isConnected && errorMessage && (
            <p className="text-center text-xs text-danger">{errorMessage}</p>
          )}
        </form>

        <hr className="border-slate-100/20" />

        <form
          onSubmit={(e) => {
            e.preventDefault();
            // onSelectBlockList?.(socketStore.blockListId || PUBLIC_BLOCK_LIST, blockListPassphrase || '');
            setErrorMessage(() => undefined);
            setWaitingForSubscription(() => true);
            socket.subscribeToBlockList();
          }}
          className="space-y-4"
        >
          <ConnectionScreen.InputGroup>
            <Select
              label="Select Block List"
              placeholder="None"
              className="w-full"
              isDisabled={!socket.isConnected || waitingForSubscription}
              scrollShadowProps={{
                isEnabled: false,
              }}
              value={socketStore.blockListId}
              selectedKeys={[socketStore.blockListId]}
              onChange={(e) => socketStore.setBlockListId(e.target.value)}
            >
              {blockLists &&
                Object.entries(blockLists).map(([key, value]) => <SelectItem key={key}>{value}</SelectItem>)}
            </Select>
          </ConnectionScreen.InputGroup>

          <ConnectionScreen.InputGroup
            className={classNames({
              hidden: !socketStore.blockListId || socketStore.blockListId === PUBLIC_BLOCK_LIST,
            })}
          >
            <Input
              type="password"
              label="Passphrase"
              placeholder="Required"
              value={socketStore.blockListPassphrase}
              isDisabled={!socket.isConnected || waitingForSubscription}
              onChange={(e) => socketStore.setBlockListPassphrase(e.target.value)}
            />
          </ConnectionScreen.InputGroup>

          <div className="flex justify-center gap-4">
            <Button
              color="primary"
              isDisabled={
                !socket.isConnected ||
                !socketStore.blockListId ||
                (socketStore.blockListId !== PUBLIC_BLOCK_LIST && !socketStore.blockListPassphrase?.length)
              }
              isLoading={waitingForSubscription}
              type="submit"
              className="flex-1"
            >
              Use Block List
            </Button>
          </div>

          {socket.isConnected && errorMessage && <p className="text-center text-xs text-danger">{errorMessage}</p>}
        </form>
      </div>
    </div>
  );
};

ConnectionScreen.InputGroup = ({ className, children }: React.HTMLAttributes<HTMLDivElement>) => (
  <div className={classNames('flex w-full flex-nowrap gap-4 md:flex-nowrap', className)}>{children}</div>
);

export default ConnectionScreen;
