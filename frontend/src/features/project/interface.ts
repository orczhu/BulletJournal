import { ProjectType } from './constants';
import { Group } from '../group/interface';

export interface Project {
  description: string;
  group: Group;
  id: number;
  name: string;
  owner: string;
  ownerAvatar: string;
  projectType: ProjectType;
  subProjects: Project[];
  shared: boolean;
}

export interface ProjectsWithOwner {
  owner: string;
  ownerAvatar: string;
  projects: Project[];
}