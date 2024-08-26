import classNames from '@/utilities/classNames';
import { XCircleIcon } from '@heroicons/react/24/outline';
import { Button, Checkbox, Input, Tooltip } from '@nextui-org/react';
import React, { useMemo } from 'react';

const DEFAULT_HOSTNAME = '127.0.0.1';
const DEFAULT_PORT = '4321';

const ConnectionScreen = ({
  onConnect,
  onCancelConnect,
  isConnecting = false,
  connectionError,
}: {
  onConnect?: (hostname: string, port: string, passphrase?: string, useSecureSocket?: boolean) => void;
  onCancelConnect?: () => void;
  isConnecting?: boolean;
  connectionError?: string;
}): React.ReactNode => {
  const isSecureConnection = useMemo(() => location.protocol.startsWith('https'), []);

  const [hostname, setHostname] = React.useState<string | undefined>(undefined);
  const [port, setPort] = React.useState<string | undefined>(undefined);
  const [passphrase, setPassphrase] = React.useState<string | undefined>(undefined);
  const [useSecureSocket, setUseSecureSocket] = React.useState<boolean>(isSecureConnection);

  React.useEffect(() => {
    setHostname(() => localStorage.getItem('hostname') ?? undefined);
    setPort(() => localStorage.getItem('port') ?? undefined);
  }, []);

  const trimmedHostname = useMemo(() => (hostname?.trim().length ? hostname?.trim() : undefined), [hostname]);
  const trimmedPort = useMemo(() => (port?.trim().length ? port?.trim() : undefined), [port]);

  const connect = () => {
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

    onConnect?.(trimmedHostname || DEFAULT_HOSTNAME, trimmedPort || DEFAULT_PORT, passphrase, useSecureSocket);
  };

  return (
    <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
      <form
        onSubmit={(e) => {
          e.preventDefault();
          connect();
        }}
        className="space-y-4 rounded-xl border-1 border-slate-100/10 p-6"
      >
        <div className="flex w-full flex-nowrap gap-4 md:flex-nowrap">
          <Input
            classNames={{
              inputWrapper: 'bg-slate-900 data-[hover=true]:bg-slate-800 group-data-[focus=true]:bg-slate-800',
            }}
            type="text"
            label="Hostname / IP address"
            placeholder={DEFAULT_HOSTNAME}
            value={hostname ?? ''}
            isDisabled={isConnecting}
            onChange={(e) => setHostname(() => e.target.value)}
          />

          <Input
            classNames={{
              inputWrapper: 'bg-slate-900 data-[hover=true]:bg-slate-800 group-data-[focus=true]:bg-slate-800',
            }}
            type="string"
            label="Port"
            placeholder={`${DEFAULT_PORT}`}
            value={port ?? ''}
            max={65535}
            maxLength={5}
            isDisabled={isConnecting}
            className="w-24 text-center"
            onChange={(e) => setPort(() => e.target.value)}
          />
        </div>

        <div className="flex w-full flex-nowrap gap-4 md:flex-nowrap">
          <Input
            type="password"
            label="Passphrase"
            placeholder="Optional"
            value={passphrase ?? ''}
            isDisabled={isConnecting}
            onChange={(e) => setPassphrase(() => e.target.value)}
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

              <small className="mt-2 text-center text-xs text-white/40">
                Your browser may display a
                <br />
                disclaimer that this site is insecure.
              </small>
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
          <Button color="primary" isDisabled={isConnecting} isLoading={isConnecting} type="submit" className="flex-1">
            {isConnecting ? `Connecting...` : 'Connect to Minecraft'}
          </Button>

          <Button
            isIconOnly
            variant="flat"
            isDisabled={!isConnecting}
            onClick={(e) => {
              e.preventDefault();
              onCancelConnect?.();
            }}
          >
            <XCircleIcon className="size-6 text-danger" />
          </Button>
        </div>

        <span
          className={classNames('block text-center text-xs text-danger', {
            invisible: !connectionError && false,
          })}
        >
          {connectionError || <>&nbsp;</>}
        </span>
      </form>
    </div>
  );
};

export default ConnectionScreen;
