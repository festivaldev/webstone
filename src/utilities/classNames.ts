import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

const classNames = (...classNames: ClassValue[]) => twMerge(clsx(classNames));

export default classNames;
