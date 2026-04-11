export type PartnerType = 'currentPartner' | 'currentMarriedPartner' | 'exPartner'

export interface PartnerRelation {
  partnerId: string
  type: PartnerType
  childIds?: string[] // children from this specific partnership
}

export interface FamilyMember {
  id: string
  firstName: string
  lastName: string
  initialLastName?: string // maiden name / birth name
  gender: 'male' | 'female' | 'diverse'
  birthDate?: string
  deathDate?: string
  birthCity?: string
  birthCountry?: string
  // Contact info (optional)
  email?: string
  telephone?: string
  streetNumber?: string
  plz?: string
  city?: string
  // Other details
  occupation?: string
  notes?: string
  imageUrl?: string
  // Relationships
  parentIds?: string[]
  spouseIds?: string[] // legacy - keeping for backwards compat
  partners?: PartnerRelation[] // new partner structure with relationship type
  childIds?: string[]
}

export interface Relationship {
  id: string
  person1Id: string
  person2Id: string
  type: 'spouse' | 'parent' | 'child' | 'sibling'
  startDate?: string
  endDate?: string
}

export const familyMembers: FamilyMember[] = [
  {
    id: '1',
    firstName: 'Giovanni',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1850-03-15',
    deathDate: '1920-11-22',
    birthCity: 'Florence',
    birthCountry: 'Italy',
    occupation: 'Merchant',
    notes: 'Patriarch of the Rossi family, immigrated to America in 1878.',
    imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['2'],
    partners: [
      { partnerId: '2', type: 'currentMarriedPartner', childIds: ['3', '4', '5'] }
    ],
    childIds: ['3', '4', '5'],
  },
  {
    id: '2',
    firstName: 'Maria',
    lastName: 'Rossi',
    initialLastName: 'Bianchi',
    gender: 'female',
    birthDate: '1855-07-08',
    deathDate: '1932-04-10',
    birthCity: 'Rome',
    birthCountry: 'Italy',
    occupation: 'Homemaker',
    notes: 'Known for her beautiful garden and traditional recipes.',
    imageUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['1'],
    partners: [
      { partnerId: '1', type: 'currentMarriedPartner', childIds: ['3', '4', '5'] }
    ],
    childIds: ['3', '4', '5'],
  },
  {
    id: '3',
    firstName: 'Antonio',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1880-01-20',
    deathDate: '1945-08-15',
    birthPlace: 'New York, USA',
    occupation: 'Carpenter',
    imageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=face',
    parentIds: ['1', '2'],
    spouseIds: ['6'],
    partners: [
      { partnerId: '6', type: 'currentMarriedPartner', childIds: ['8', '9'] }
    ],
    childIds: ['8', '9'],
  },
  {
    id: '4',
    firstName: 'Elena',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1882-06-12',
    deathDate: '1960-03-28',
    birthPlace: 'New York, USA',
    occupation: 'Teacher',
    imageUrl: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop&crop=face',
    parentIds: ['1', '2'],
    spouseIds: ['7'],
    childIds: ['10'],
  },
  {
    id: '5',
    firstName: 'Marco',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1885-09-03',
    deathDate: '1918-10-15',
    birthPlace: 'New York, USA',
    occupation: 'Soldier',
    bio: 'Served in WWI, died at the Battle of Vittorio Veneto.',
    imageUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop&crop=face',
    parentIds: ['1', '2'],
  },
  {
    id: '6',
    firstName: 'Sofia',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1885-02-14',
    deathDate: '1955-12-01',
    birthPlace: 'Boston, USA',
    occupation: 'Seamstress',
    imageUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['3'],
    childIds: ['8', '9'],
  },
  {
    id: '7',
    firstName: 'James',
    lastName: 'Miller',
    gender: 'male',
    birthDate: '1878-11-30',
    deathDate: '1950-07-20',
    birthPlace: 'Philadelphia, USA',
    occupation: 'Banker',
    imageUrl: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['4'],
    childIds: ['10'],
  },
  {
    id: '8',
    firstName: 'Giuseppe',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1910-04-22',
    deathDate: '1985-09-14',
    birthPlace: 'New York, USA',
    occupation: 'Doctor',
    imageUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200&h=200&fit=crop&crop=face',
    parentIds: ['3', '6'],
    spouseIds: ['11'],
    childIds: ['13', '14'],
  },
  {
    id: '9',
    firstName: 'Lucia',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1912-08-05',
    deathDate: '1990-02-18',
    birthPlace: 'New York, USA',
    occupation: 'Nurse',
    imageUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&crop=face',
    parentIds: ['3', '6'],
    spouseIds: ['12'],
    childIds: ['15'],
  },
  {
    id: '10',
    firstName: 'William',
    lastName: 'Miller',
    gender: 'male',
    birthDate: '1915-12-25',
    deathDate: '2000-06-30',
    birthPlace: 'New York, USA',
    occupation: 'Lawyer',
    imageUrl: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=200&h=200&fit=crop&crop=face',
    parentIds: ['4', '7'],
    spouseIds: ['16'],
    childIds: ['17'],
  },
  {
    id: '11',
    firstName: 'Rose',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1915-05-10',
    deathDate: '1995-11-22',
    birthPlace: 'Brooklyn, USA',
    occupation: 'Artist',
    imageUrl: 'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['8'],
    childIds: ['13', '14'],
  },
  {
    id: '12',
    firstName: 'Thomas',
    lastName: 'O\'Brien',
    gender: 'male',
    birthDate: '1908-03-17',
    deathDate: '1975-08-02',
    birthPlace: 'Boston, USA',
    occupation: 'Factory Worker',
    imageUrl: 'https://images.unsplash.com/photo-1504257432389-52343af06ae3?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['9'],
    childIds: ['15'],
  },
  {
    id: '13',
    firstName: 'Michael',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1940-07-04',
    birthCity: 'New York',
    birthCountry: 'USA',
    email: 'michael.rossi@university.edu',
    occupation: 'Professor',
    notes: 'Emeritus professor of History at Columbia University.',
    imageUrl: 'https://images.unsplash.com/photo-1463453091185-61582044d556?w=200&h=200&fit=crop&crop=face',
    parentIds: ['8', '11'],
    spouseIds: ['18', '23'],
    partners: [
      { partnerId: '23', type: 'exPartner', childIds: ['24'] },
      { partnerId: '18', type: 'currentMarriedPartner', childIds: ['20', '21'] }
    ],
    childIds: ['20', '21', '24'],
  },
  {
    id: '14',
    firstName: 'Anna',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1945-02-28',
    birthPlace: 'New York, USA',
    occupation: 'Writer',
    imageUrl: 'https://images.unsplash.com/photo-1489424731084-a5d8b219a5bb?w=200&h=200&fit=crop&crop=face',
    parentIds: ['8', '11'],
  },
  {
    id: '15',
    firstName: 'Patrick',
    lastName: 'O\'Brien',
    gender: 'male',
    birthDate: '1942-09-12',
    deathDate: '2020-01-15',
    birthPlace: 'New York, USA',
    occupation: 'Engineer',
    imageUrl: 'https://images.unsplash.com/photo-1507591064344-4c6ce005b128?w=200&h=200&fit=crop&crop=face',
    parentIds: ['9', '12'],
    spouseIds: ['19'],
    childIds: ['22'],
  },
  {
    id: '16',
    firstName: 'Elizabeth',
    lastName: 'Miller',
    gender: 'female',
    birthDate: '1920-04-15',
    deathDate: '2005-12-10',
    birthPlace: 'Connecticut, USA',
    occupation: 'Homemaker',
    imageUrl: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['10'],
    childIds: ['17'],
  },
  {
    id: '17',
    firstName: 'Robert',
    lastName: 'Miller',
    gender: 'male',
    birthDate: '1950-06-18',
    birthPlace: 'New York, USA',
    occupation: 'Architect',
    imageUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=face',
    parentIds: ['10', '16'],
  },
  {
    id: '18',
    firstName: 'Catherine',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1945-10-31',
    birthPlace: 'Chicago, USA',
    occupation: 'Musician',
    imageUrl: 'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['13'],
    partners: [
      { partnerId: '13', type: 'currentMarriedPartner', childIds: ['20', '21'] }
    ],
    childIds: ['20', '21'],
  },
  {
    id: '19',
    firstName: 'Margaret',
    lastName: 'O\'Brien',
    gender: 'female',
    birthDate: '1948-03-08',
    birthPlace: 'New York, USA',
    occupation: 'Accountant',
    imageUrl: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200&h=200&fit=crop&crop=face',
    spouseIds: ['15'],
    childIds: ['22'],
  },
  {
    id: '20',
    firstName: 'David',
    lastName: 'Rossi',
    gender: 'male',
    birthDate: '1975-11-20',
    birthCity: 'New York',
    birthCountry: 'USA',
    email: 'david.rossi@email.com',
    telephone: '+1 212 555 0123',
    streetNumber: '245 Park Avenue',
    plz: '10167',
    city: 'New York',
    occupation: 'Software Engineer',
    notes: 'Works at a tech startup in Manhattan. Enjoys hiking and photography.',
    imageUrl: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&h=200&fit=crop&crop=face',
    parentIds: ['13', '18'],
  },
  {
    id: '21',
    firstName: 'Sarah',
    lastName: 'Rossi',
    gender: 'diverse',
    birthDate: '1978-06-15',
    birthCity: 'New York',
    birthCountry: 'USA',
    email: 'sarah.rossi@newsoutlet.com',
    telephone: '+1 212 555 0456',
    occupation: 'Journalist',
    notes: 'Foreign correspondent covering European affairs.',
    imageUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&h=200&fit=crop&crop=face',
    parentIds: ['13', '18'],
  },
  {
    id: '22',
    firstName: 'Kevin',
    lastName: 'O\'Brien',
    gender: 'male',
    birthDate: '1972-08-22',
    birthPlace: 'New York, USA',
    occupation: 'Chef',
    imageUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200&h=200&fit=crop&crop=face',
    parentIds: ['15', '19'],
  },
  {
    id: '23',
    firstName: 'Linda',
    lastName: 'Chen',
    gender: 'female',
    birthDate: '1942-09-18',
    birthPlace: 'San Francisco, USA',
    occupation: 'Researcher',
    imageUrl: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=200&h=200&fit=crop&crop=face',
    partners: [
      { partnerId: '13', type: 'exPartner', childIds: ['24'] }
    ],
    childIds: ['24'],
  },
  {
    id: '24',
    firstName: 'Emily',
    lastName: 'Rossi',
    gender: 'female',
    birthDate: '1968-03-12',
    birthPlace: 'Boston, USA',
    occupation: 'Therapist',
    imageUrl: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200&h=200&fit=crop&crop=face',
    parentIds: ['13', '23'],
  },
]

export const getYearRange = () => {
  const years: number[] = []
  for (let year = 1850; year <= 2025; year++) {
    years.push(year)
  }
  return years
}

export const getMembersAliveInYear = (year: number): FamilyMember[] => {
  return familyMembers.filter(member => {
    const birthYear = new Date(member.birthDate).getFullYear()
    const deathYear = member.deathDate ? new Date(member.deathDate).getFullYear() : 2025
    return birthYear <= year && year <= deathYear
  })
}

export const getMemberById = (id: string): FamilyMember | undefined => {
  return familyMembers.find(m => m.id === id)
}

export const getStatistics = () => {
  return {
    totalMembers: familyMembers.length,
    generations: 5,
    oldestRecord: '1850',
    locations: ['Italy', 'New York', 'Boston', 'Philadelphia', 'Chicago', 'Connecticut'],
  }
}

// Get all descendants from a head member (including the head, their spouse, children, grandchildren, etc.)
export const getDescendantsFromHead = (headId: string): FamilyMember[] => {
  const head = getMemberById(headId)
  if (!head) return []
  
  const visited = new Set<string>()
  const result: FamilyMember[] = []
  
  function collectDescendants(memberId: string) {
    if (visited.has(memberId)) return
    visited.add(memberId)
    
    const member = getMemberById(memberId)
    if (!member) return
    
    result.push(member)
    
    // Add spouse(s)
    member.spouseIds?.forEach(spouseId => {
      if (!visited.has(spouseId)) {
        const spouse = getMemberById(spouseId)
        if (spouse) {
          visited.add(spouseId)
          result.push(spouse)
        }
      }
    })
    
    // Recurse into children
    member.childIds?.forEach(childId => {
      collectDescendants(childId)
    })
  }
  
  collectDescendants(headId)
  return result
}

// Calculate generations from head (counting down the tree)
export const getGenerationsFromHead = (headId: string): number => {
  const head = getMemberById(headId)
  if (!head) return 0
  
  function getDepth(memberId: string): number {
    const member = getMemberById(memberId)
    if (!member || !member.childIds?.length) return 1
    
    const childDepths = member.childIds.map(childId => getDepth(childId))
    return 1 + Math.max(...childDepths)
  }
  
  return getDepth(headId)
}
