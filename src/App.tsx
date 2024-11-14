import { Spinner } from '@nextui-org/react';
import React from 'react';
import { BlockGroup, ConnectionScreen, Header } from './components';
import { useSocket, useSocketListeners } from './components/SocketProvider';
import { ModalProvider } from './components/modal/ModalProvider';

const App = () => {
  const socket = useSocket();
  const socketListener = useSocketListeners();

  const [blockLists, setBlockLists] = React.useState<any>({});

  const [blockGroups, setBlockGroups] = React.useState<BlockGroup[] | undefined>([]);
  const [blocks, setBlocks] = React.useState<Block[] | undefined>();
  const ungroupedBlocks = React.useMemo<Block[]>(() => blocks?.filter((block) => !block.groupId) ?? [], [blocks]);

  React.useEffect(() => {
    const onOpen = (_: Event): void => {};

    const onClose = (_: Event): void => {
      setBlockLists(() => {});
      setBlockGroups(() => []);
      setBlocks(() => []);

      socket.setIsSubscribed(() => false);
    };

    const onMessage = (e: Event): void => {
      const { data } = e as MessageEvent;
      // console.log(`IN: ${JSON.stringify(data)}`);

      switch (data.type) {
        case 'WELCOME':
          socket.send('AUTH_REQ', {
            passphrase: socket.store.getState().socketPassphrase,
          });
          break;
        case 'UNSUBSCRIBE':
          socket.setIsSubscribed(() => false);
          setBlocks(() => []);
          setBlockGroups(() => []);
          break;
        case 'BLOCK_LISTS':
          setBlockLists(() => data.payload.blockLists);
          break;
        case 'BLOCKS':
          setBlocks(() => data.payload.blocks);
          break;
        case 'BLOCK_GROUPS':
          setBlockGroups(() => data.payload.blockGroups);
          break;
        case 'BLOCK_UPDATE':
          setBlocks((blocks) =>
            blocks?.map((block) => (block.blockId === data.payload.blockId ? { ...data.payload } : block)),
          );
          break;
        case 'BLOCK_GROUP_UPDATE':
          setBlockGroups((blockGroups) =>
            blockGroups?.map((blockGroup) =>
              blockGroup.groupId === data.payload.groupId ? { ...data.payload } : blockGroup,
            ),
          );
          break;
        default:
          break;
      }
    };

    const onError = (e: Event): void => {
      const { message } = e as ErrorEvent;
      console.log(`ERROR: ${message}`);
    };

    socketListener.addEventListener('open', onOpen);
    socketListener.addEventListener('close', onClose);
    socketListener.addEventListener('message', onMessage);
    socketListener.addEventListener('error', onError);

    return () => {
      socketListener.removeEventListener('open', onOpen);
      socketListener.removeEventListener('close', onClose);
      socketListener.removeEventListener('message', onMessage);
      socketListener.removeEventListener('error', onError);
    };
  }, []);

  return (
    <ModalProvider blocks={blocks} blockGroups={blockGroups}>
      <div className="p-4 pt-20">
        <Header blockGroups={blockGroups} />

        {!socket.isSubscribed && <ConnectionScreen blockLists={blockLists} />}

        {socket.isSubscribed && (
          <>
            {(!blockGroups || !blocks) && (
              <div className="absolute bottom-0 left-0 right-0 top-0 flex flex-col items-center justify-center">
                <Spinner size="lg" />
              </div>
            )}

            {blockGroups?.length === 0 && blocks?.length === 0 && (
              <div className="absolute left-0 top-0 flex h-full w-full flex-col items-center justify-center">
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
              {ungroupedBlocks && ungroupedBlocks.length > 0 && <BlockGroup blocks={ungroupedBlocks} />}

              {blockGroups?.map((blockGroup) => (
                <BlockGroup
                  key={blockGroup.groupId}
                  group={blockGroup}
                  blocks={
                    (blockGroup.blockIds
                      .map((id) => blocks?.find((block) => id === block.blockId))
                      .filter(Boolean) as Block[]) ?? []
                  }
                />
              ))}
            </div>
          </>
        )}
      </div>
    </ModalProvider>
  );
};

export default App;
