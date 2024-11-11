import { classNames } from '@/utilities';
import {
  ArrowTopRightOnSquareIcon,
  EllipsisVerticalIcon,
  FolderOpenIcon,
  PlusIcon,
  PowerIcon,
  QueueListIcon,
} from '@heroicons/react/24/outline';
import { SiCurseforge, SiGithub, SiModrinth } from '@icons-pack/react-simple-icons';
import {
  Button,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownSection,
  DropdownTrigger,
  Tooltip,
} from '@nextui-org/react';
import React from 'react';
import { useSocket } from './SocketProvider';
import { useModals } from './modal/ModalProvider';

const Header = ({ blockGroups }: { blockGroups: BlockGroup[] | undefined }): React.ReactNode => {
  const { isConnected, isSubscribed, disconnect, unsubscribeFromBlockList } = useSocket();
  const { openCreateGroupModal, openChangeGroupOrderModal } = useModals();

  const showAppSection = React.useMemo(() => isConnected && isSubscribed, [isConnected, isSubscribed]);
  const reorderGroupsDisabled = React.useMemo(() => blockGroups?.length === 0, [blockGroups]);

  const disabledKeys = React.useMemo<string[]>(
    () => [reorderGroupsDisabled ? 'reorder-groups' : undefined].filter(Boolean) as string[],
    [reorderGroupsDisabled],
  );

  return (
    <header className="fixed left-0 right-0 top-0 z-50 flex items-center justify-between border-b-1 border-slate-100/10 bg-slate-950/60 p-2 backdrop-blur-lg">
      <a href="/" className="flex items-center gap-3">
        <img src="/icon.png" className="size-8" />
        <h1 className="text-lg font-semibold md:text-xl md:font-medium">Webstone</h1>
      </a>

      <Dropdown>
        <Tooltip showArrow closeDelay={0} content="Options">
          <div>
            <DropdownTrigger>
              <Button isIconOnly variant="light">
                <EllipsisVerticalIcon className="size-5" />
              </Button>
            </DropdownTrigger>
          </div>
        </Tooltip>

        <DropdownMenu disabledKeys={disabledKeys}>
          <DropdownSection
            showDivider={showAppSection}
            classNames={{
              base: classNames({ 'mb-0': !showAppSection }),
              group: 'flex flex-col gap-1',
            }}
          >
            <DropdownItem
              key="curseforge"
              startContent={<SiCurseforge className="size-5" />}
              endContent={<ArrowTopRightOnSquareIcon className="size-4" />}
              href="https://www.curseforge.com/minecraft/mc-mods/webstone"
              target="_blank"
            >
              CurseForge
            </DropdownItem>
            <DropdownItem
              key="modrinth"
              startContent={<SiModrinth className="size-5" />}
              endContent={<ArrowTopRightOnSquareIcon className="size-4" />}
              href="https://modrinth.com/mod/webstone"
              target="_blank"
            >
              Modrinth
            </DropdownItem>
            <DropdownItem
              key="github"
              startContent={<SiGithub className="size-5" />}
              endContent={<ArrowTopRightOnSquareIcon className="size-4" />}
              href="https://github.com/festivaldev/webstone/tree/webui"
              target="_blank"
            >
              GitHub
            </DropdownItem>
          </DropdownSection>

          <DropdownSection
            hidden={!showAppSection}
            classNames={{
              base: 'mb-0',
              group: 'flex flex-col gap-1',
            }}
          >
            <DropdownItem
              key="create-group"
              startContent={<PlusIcon className="size-5" />}
              onClick={openCreateGroupModal}
            >
              Create Group
            </DropdownItem>
            <DropdownItem
              key="reorder-groups"
              startContent={<QueueListIcon className="size-5" />}
              onClick={openChangeGroupOrderModal}
            >
              Reorder Groups
            </DropdownItem>
            <DropdownItem
              key="change-block-list"
              startContent={<FolderOpenIcon className="size-5" />}
              onClick={unsubscribeFromBlockList}
            >
              Change Block List
            </DropdownItem>
            <DropdownItem
              key="delete"
              className="text-danger"
              color="danger"
              startContent={<PowerIcon className="size-5" />}
              onClick={disconnect}
            >
              Disconnect
            </DropdownItem>
          </DropdownSection>
        </DropdownMenu>
      </Dropdown>
    </header>
  );
};

export default Header;
