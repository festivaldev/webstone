import classNames from '@/utilities/classNames';
import { EllipsisHorizontalIcon, PencilSquareIcon, QueueListIcon, TrashIcon } from '@heroicons/react/24/outline';
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownTrigger,
  Tooltip,
} from '@nextui-org/react';
import BlockButton from './BlockButton';
import { useModals } from './modal/ModalProvider';

const BlockGroup = ({ group, blocks }: { group?: BlockGroup; blocks: Block[] }): React.ReactNode => {
  const { openRenameGroupModal, openChangeBlockOrderModal, openDeleteGroupModal } = useModals();

  return (
    <Card
      className={classNames({
        'border-0 bg-transparent shadow-none': !group,
      })}
      classNames={{
        body: classNames({
          'p-0': !group,
        }),
      }}
    >
      {group && (
        <CardHeader className="flex items-center justify-between border-b-1 border-slate-100/10">
          <h4 className="text-xl font-medium text-white/90">{group.name}</h4>

          <div>
            <Dropdown showArrow>
              <Tooltip showArrow closeDelay={0} content="Group Options">
                <div>
                  <DropdownTrigger>
                    <Button isIconOnly variant="light">
                      <EllipsisHorizontalIcon className="size-5" />
                    </Button>
                  </DropdownTrigger>
                </div>
              </Tooltip>
              <DropdownMenu>
                <DropdownItem
                  key="rename"
                  startContent={<PencilSquareIcon className="size-5" />}
                  onClick={() => openRenameGroupModal(group.groupId)}
                >
                  Rename Group
                </DropdownItem>
                <DropdownItem
                  key="reorder-blocks"
                  startContent={<QueueListIcon className="size-5" />}
                  onClick={() => openChangeBlockOrderModal(group.groupId)}
                >
                  Reorder Blocks
                </DropdownItem>
                <DropdownItem
                  key="delete"
                  className="text-danger"
                  color="danger"
                  startContent={<TrashIcon className="size-5" />}
                  onClick={() => openDeleteGroupModal(group.groupId)}
                >
                  Delete Group
                </DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </div>
        </CardHeader>
      )}

      <CardBody>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 2xl:grid-cols-8 4xl:grid-cols-12">
          {blocks.length === 0 && (
            <div className="col-span-2 sm:col-span-3 md:col-span-4 lg:col-span-6 2xl:col-span-8 4xl:col-span-12">
              <p className="py-3 text-center text-lg text-gray-500 md:py-6 md:text-xl">
                No blocks assigned to this group.
                <br />
                <span className="mt-1 block text-sm leading-tight md:mt-2 md:text-base">
                  Assign a block to this group by clicking{' '}
                  <EllipsisHorizontalIcon className="inline size-4 md:size-5" />
                  <br />
                  in any block and choosing "Change Group".
                </span>
              </p>
            </div>
          )}

          {blocks.map((block) => (
            <BlockButton key={block.blockId} block={block} />
          ))}
        </div>
      </CardBody>
    </Card>
  );
};

export default BlockGroup;
