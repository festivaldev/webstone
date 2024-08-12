import { PencilIcon, TrashIcon } from '@heroicons/react/24/outline';
import { Slider } from '@nextui-org/react';
import { useMemo, useState } from 'react';
import classNames from './utilities/classNames';

interface Block {
  blockId: string;
  name: string;
  powered: boolean;
  power: number;
}

const App = () => {
  const [hostname, setHostname] = useState<string | undefined>(undefined);
  const [port, setPort] = useState<number | undefined>(undefined);

  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [connected, setConnected] = useState<boolean>(false);
  const [blocks, setBlocks] = useState<Block[]>([
    // {
    //   blockId: '0',
    //   name: 'debug',
    //   power: 15,
    //   powered: true,
    // },
  ]);

  const connecting = useMemo(() => socket != null && !connected, [socket, connected]);

  const connect = (hostname: string = '127.0.0.1', port: number = 4321) => {
    const _socket = new WebSocket(`ws://${hostname}:${port}`);

    // Connection opened
    _socket.addEventListener('open', () => {
      console.log('Connection opened');
      setConnected(() => true);
    });

    _socket.addEventListener('close', () => {
      console.log('Connection closed');
      setSocket(() => null);
      setConnected(() => false);
    });

    _socket.addEventListener('error', () => {
      alert(`Failed to connect to ${hostname}:${port}!`);
    });

    _socket.addEventListener('message', ({ data: message }) => {
      try {
        const data = JSON.parse(message);
        // console.log(data);

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
      } catch {
        //
      }
    });

    setSocket(() => _socket);
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
            name: blockName.trim(),
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

  if (!connected)
    return (
      <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
        <div className="space-x-3">
          <input
            type="text"
            placeholder="127.0.0.1"
            value={hostname ?? ''}
            onChange={(e) => setHostname(() => e.target.value)}
            className="rounded-lg border border-slate-100/30 bg-transparent px-3 py-2 focus:bg-white focus:text-black focus:outline-none"
          />
          <input
            type="number"
            placeholder="4321"
            value={port ?? ''}
            onChange={(e) => !isNaN(parseInt(e.target.value)) && setPort(() => Number(e.target.value))}
            className="w-16 rounded-lg border border-slate-100/30 bg-transparent px-3 py-2 [appearance:textfield] focus:bg-white focus:text-black focus:outline-none [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
          />
        </div>
        <button
          className="rounded-lg bg-sky-700 px-4 py-2 disabled:opacity-40"
          disabled={connecting}
          onClick={() => connect(hostname, port)}
        >
          {connecting ? 'Connecting...' : 'Connect to Minecraft'}
        </button>
      </div>
    );

  return (
    <div className="h-full w-full p-4">
      {blocks.length == 0 && (
        <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center space-y-4">
          <p className="text-center text-xl text-gray-500">
            No Webstone blocks registered.
            <br />
            <span className="text-base">Place a Webstone block and right-click it with an empty hand to register.</span>
          </p>
        </div>
      )}
      {blocks.length > 0 && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 2xl:grid-cols-8 3xl:grid-cols-12">
          {blocks.map((block) => (
            <div
              key={block.blockId}
              className={classNames(
                'relative flex aspect-square select-none flex-col rounded-lg bg-slate-700 p-3 text-center transition-colors',
                {
                  'bg-green-700': block.powered,
                },
              )}
            >
              <div className="flex justify-end gap-3">
                <button
                  className="rounded-md bg-slate-100/10 p-2 transition-colors duration-100 hover:bg-sky-600"
                  onClick={(e) => {
                    e.stopPropagation();
                    renameBlock(block.blockId);
                  }}
                >
                  <PencilIcon className="size-5" />
                </button>
                <button
                  className="rounded-md bg-slate-100/10 p-2 transition-colors duration-100 hover:bg-sky-600"
                  onClick={(e) => {
                    e.stopPropagation();
                    unregisterBlock(block.blockId);
                  }}
                >
                  <TrashIcon className="size-5" />
                </button>
              </div>

              <div
                className="flex flex-1 cursor-pointer items-center justify-center"
                onClick={() => setBlockState(block.blockId, !block.powered)}
              >
                <span className="my-2 text-xl font-semibold">{block.name}</span>
              </div>

              <div>
                <Slider
                  color="danger"
                  size="md"
                  step={1}
                  label="Strength"
                  maxValue={15}
                  minValue={0}
                  defaultValue={15}
                  value={block.power}
                  // onChange={(value) =>
                  //   setBlocks((blocks) =>
                  //     blocks.map((_) => (_.blockId === block.blockId ? { ..._, power: value as number } : _)),
                  //   )
                  // }
                  onChange={(value) => setBlockPower(block.blockId, value as number)}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default App;
