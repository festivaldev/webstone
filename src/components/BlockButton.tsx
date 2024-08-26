import classNames from '@/utilities/classNames';
import { EllipsisHorizontalIcon, ListBulletIcon, PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline';
import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownTrigger,
  Tooltip,
} from '@nextui-org/react';
import Slider from './Slider';

const BlockButton = ({
  block,
  onStateChanged,
  onPowerChanged,
  onRename,
  onGroupChange,
  onDelete,
}: {
  block: Block;
  onStateChanged?: (powered: boolean) => void;
  onPowerChanged?: (power: number) => void;
  onRename?: () => void;
  onGroupChange?: () => void;
  onDelete?: () => void;
}): React.ReactNode => (
  <>
    <Card
      as="div"
      isPressable
      isHoverable={false}
      className={classNames('aspect-square bg-slate-800', {
        'bg-green-600': block.powered,
      })}
      onClick={() => onStateChanged?.(!block.powered)}
    >
      <CardHeader className="flex justify-end gap-0.5 pb-0 md:gap-1">
        <Dropdown>
          <DropdownTrigger>
            <Button isIconOnly variant="light" className="border-1 border-slate-100/10">
              <EllipsisHorizontalIcon className="size-5" />
            </Button>
          </DropdownTrigger>
          <DropdownMenu>
            <DropdownItem key="rename" startContent={<PencilSquareIcon className="size-5" />} onClick={onRename}>
              Rename Block
            </DropdownItem>
            <DropdownItem
              key="switch-group"
              startContent={<ListBulletIcon className="size-5" />}
              onClick={onGroupChange}
            >
              Change Group
            </DropdownItem>
            <DropdownItem
              key="delete"
              className="text-danger"
              color="danger"
              startContent={<TrashIcon className="size-5" />}
              onClick={onDelete}
            >
              Delete Block
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </CardHeader>

      <CardBody className="flex justify-center py-2 text-center">
        <Tooltip showArrow placement="bottom" closeDelay={0} content={block.name} hidden={block.name.length < 20}>
          <span className="overflow-hidden overflow-ellipsis font-semibold md:text-xl">{block.name}</span>
        </Tooltip>
      </CardBody>

      <CardFooter
        className="cursor-default px-4 pt-0"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
        }}
      >
        <Slider
          color={block.powered ? 'danger' : 'test'}
          size="md"
          step={1}
          label="Strength"
          maxValue={15}
          minValue={0}
          defaultValue={15}
          value={block.power}
          onChange={(value) => onPowerChanged?.(value as number)}
        />
      </CardFooter>
    </Card>
  </>
);

export default BlockButton;
