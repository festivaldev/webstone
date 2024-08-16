import icon from '@/icon.png';
import classNames from '@/utilities/classNames';
import { ArrowTopRightOnSquareIcon, EllipsisVerticalIcon, PowerIcon } from '@heroicons/react/24/outline';
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
import { type MouseEventHandler } from 'react';

const Header = ({
  isConnected,
  disconnect,
}: {
  isConnected: boolean;
  disconnect: MouseEventHandler<HTMLElement>;
}): React.ReactNode => (
  <header className="fixed left-0 top-0 z-50 flex w-full items-center justify-between border-b-1 border-b-slate-100/10 bg-slate-950/60 p-2 backdrop-blur-lg">
    <div className="flex items-center gap-3">
      <img src={icon} className="size-8" />
      <h1 className="text-lg font-semibold md:text-xl md:font-medium">Webstone</h1>
    </div>

    <div className="flex gap-2 justify-self-end">
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
        <DropdownMenu>
          <DropdownSection
            showDivider={isConnected}
            classNames={{
              base: classNames({ 'mb-0': !isConnected }),
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

          <DropdownSection hidden={!isConnected} classNames={{ base: 'mb-0' }}>
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
    </div>
  </header>
);

export default Header;
